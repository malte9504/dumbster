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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.dumbster.smtp.mailstores.RollingMailStore;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerOptions
{
    public static final int SMTP_PORT = 25;
    // options
    private int port = SMTP_PORT;
    private boolean threaded = true;
    private MailStore mailStore = new RollingMailStore();
    private int waitInResponse = 0;

    public ServerOptions()
    {}

    public ServerOptions(String[] args)
    {
        if (args.length == 0) {
            return;
        }

        for (String argument : args) {
            if (argument.startsWith("--mailStore")) {
                String[] values = argument.split("=");
                if (values.length != 2) {
                    throw new IllegalArgumentException("--mailStore must have an argument");
                }

                String className = "com.dumbster.smtp.mailstores." + values[1];
                try {
                    this.withMailStore((MailStore) Class.forName(className).newInstance());
                }
                catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("Could not instantiate mail store class: " + className, e);
                }
            }
            else if (argument.startsWith("--threaded")) {
                if (argument.equalsIgnoreCase("--threaded=false")) {
                    this.notThreaded();
                }
                else {
                    this.threaded();
                }
            }
            else {
                try {
                    this.withSmtpPort(Integer.parseInt(argument));
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException("'" + argument + "' is not a valid number");
                }
            }
        }
    }

    public ServerOptions threaded()
    {
        this.threaded = true;

        return this;
    }

    public ServerOptions notThreaded()
    {
        this.threaded = false;

        return this;
    }

    public ServerOptions withMailStore(MailStore mailStore)
    {
        requireNonNull(mailStore, "mailStore is null");
        this.mailStore = mailStore;

        return this;
    }

    public ServerOptions withDefaultSmtpPort()
    {
        this.port = SMTP_PORT;

        return this;
    }

    public ServerOptions withRandomSmtpPort()
    {
        this.port = randomPort();

        return this;
    }

    public ServerOptions withSmtpPort(int port)
    {
        if (port <= 0 || port > 65534) {
            throw new IllegalArgumentException(format("Port %d must be > 0 and < 65535", port));
        }

        this.port = port;

        return this;
    }

    public ServerOptions withWaitInResponseInMs(int waitInResponse)
    {
        if (waitInResponse < 0) {
            throw new IllegalArgumentException("waitInResponse must be > 0");
        }

        this.waitInResponse = waitInResponse;

        return this;
    }

    public int getPort()
    {
        return port;
    }

    public boolean isThreaded()
    {
        return threaded;
    }

    public MailStore getMailStore()
    {
        return mailStore;
    }

    public int getWaitInResponse()
    {
        return waitInResponse;
    }

    private static int randomPort()
    {
        try (ServerSocket socket = new ServerSocket(0);) {
            return socket.getLocalPort();
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not allocate a free port", e);
        }
    }
}
