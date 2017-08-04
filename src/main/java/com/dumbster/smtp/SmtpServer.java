/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dummy SMTP server for testing purposes.
 */
public class SmtpServer implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(SmtpServer.class);

    // Timeout for socket accept. Do *not* set to 0 (call will block forever)
    private static final int SERVER_SOCKET_TIMEOUT = 10;
    private static final int MAX_THREADS = 10;

    // True if the server is accepting connections.
    private volatile boolean running = false;

    // True if the server is not active.
    private volatile boolean stopped = true;

    private volatile Thread serverThread = null;

    private final int port;
    private final MailStore mailStore;
    private final boolean threaded;
    private final int waitInResponse;


    public SmtpServer(final ServerOptions serverOptions)
    {
        requireNonNull(serverOptions, "serverOptions is null");
        this.port = serverOptions.getPort();
        this.mailStore = serverOptions.getMailStore();
        this.threaded = serverOptions.isThreaded();
        this.waitInResponse = serverOptions.getWaitInResponse();
    }

    @Override
    public void run()
    {
        serverThread = Thread.currentThread();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT);
            serverLoop(serverSocket);
        }
        catch (IOException e) {
            LOG.warn("Server Loop terminated: ", e);
        }
    }

    private void serverLoop(ServerSocket serverSocket) throws IOException
    {
        ExecutorService executorService;

        final String serverThreadName = Thread.currentThread().getName();

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadId = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r)
            {
                Thread serviceThread = new Thread(r);
                serviceThread.setDaemon(true);
                serviceThread.setName(serverThreadName + "-" + threadId.getAndIncrement());
                return serviceThread;
            }
        };

        if (threaded) {
            executorService = Executors.newFixedThreadPool(MAX_THREADS, threadFactory);
        }
        else {
            executorService = Executors.newSingleThreadExecutor(threadFactory);
        }

        this.running = true;
        this.stopped = false;

        do {
            try {
                Socket clientSocket = serverSocket.accept();
                SocketWrapper source = new SocketWrapper(clientSocket);
                ClientSession session;

                if (waitInResponse == 0) {
                    session = new ClientSession(source, mailStore);
                }
                else {
                    session = new TimedClientSession(source, mailStore, waitInResponse);
                }

                executorService.execute(session);
            }
            catch (SocketTimeoutException e) {
                LOG.trace("Tick ...");
            }
            catch (IOException e) {
                // Don't bother logging if we shut down, it is probably a
                // socket closed exception
                if (isRunning()) {
                    LOG.warn("In accept loop: ", e);
                }
            }
        }
        while (isRunning());

        executorService.shutdown();
        stopped = true;
    }

    public boolean isStopped()
    {
        return stopped;
    }

    public boolean isRunning()
    {
        return running;
    }

    public synchronized void stop() throws InterruptedException
    {
        // exit the accept loop.
        running = false;
        if (serverThread != null) {
            serverThread.interrupt();
        }

        serverThread.join();
    }

    public MailMessage[] getMessages()
    {
        return mailStore.getMessages();
    }

    public MailMessage getMessage(int i)
    {
        return mailStore.getMessage(i);
    }

    public int getEmailCount()
    {
        return mailStore.getEmailCount();
    }

    public void anticipateMessageCountFor(int messageCount, int ticks)
    {
        int tickdown = ticks;
        while (mailStore.getEmailCount() < messageCount && tickdown > 0) {
            tickdown--;
            try {
                Thread.sleep(1);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void clearMessages()
    {
        this.mailStore.clearMessages();
    }

    public int getPort()
    {
        return port;
    }
}
