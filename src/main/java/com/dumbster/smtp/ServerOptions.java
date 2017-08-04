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

import com.dumbster.smtp.mailstores.RollingMailStore;

public class ServerOptions
{
    public int port = SmtpServer.DEFAULT_SMTP_PORT;
    public boolean threaded = true;
    public MailStore mailStore = new RollingMailStore();
    public boolean valid = true;
    public int waitInResponse = 0;

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
                    this.valid = false;
                    return;
                }
                try {
                    this.mailStore = (MailStore) Class.forName("com.dumbster.smtp.mailstores." + values[1]).newInstance();
                }
                catch (ReflectiveOperationException e) {
                    this.valid = false;
                    return;
                }
            }
            else if (argument.startsWith("--threaded")) {
                this.threaded = !argument.equalsIgnoreCase("--threaded=false");
            }
            else {
                try {
                    this.port = Integer.parseInt(argument);
                }
                catch (NumberFormatException e) {
                    this.valid = false;
                    break;
                }
            }
        }
    }
}
