/*
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

/**
 * User: rj
 * Date: Aug 28, 2011
 * Time: 6:48:14 AM
 */
public class SmtpServerFactory
{
    public static SmtpServer startServer()
    {
        ServerOptions serverOptions = new ServerOptions();
        return startServer(serverOptions);
    }

    public static SmtpServer startServer(ServerOptions options)
    {
        SmtpServer server = wireUpServer(options);
        wrapInShutdownHook(server);
        startServerThread(server);
        System.out.println("Dumbster SMTP Server started on port " + options.port + ".\n");
        return server;
    }

    private static SmtpServer wireUpServer(ServerOptions options)
    {
        SmtpServer server = new SmtpServer();
        server.setPort(options.port);
        server.setThreaded(options.threaded);
        server.setMailStore(options.mailStore);
        return server;
    }

    private static void wrapInShutdownHook(final SmtpServer server)
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                server.stop();
                System.out.println("\nDumbster SMTP Server stopped");
                System.out.println("\tTotal messages received: " + server.getEmailCount());
            }
        });
    }

    private static void startServerThread(SmtpServer server)
    {
        new Thread(server).start();
        int timeout = 1000;
        while (!server.isReady()) {
            try {
                Thread.sleep(1);
                timeout--;
                if (timeout < 1) {
                    throw new RuntimeException("Server could not be started.");
                }
            }
            catch (InterruptedException ignored) {
            }
        }
    }
}
