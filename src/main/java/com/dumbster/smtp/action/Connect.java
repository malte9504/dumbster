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
package com.dumbster.smtp.action;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;
import com.dumbster.smtp.Response;
import com.dumbster.smtp.SmtpState;

public class Connect implements Action
{

    @Override
    public String toString()
    {
        return "Connect";
    }

    @Override
    public Response response(SmtpState smtpState, MailStore mailStore, MailMessage currentMessage)
    {
        if (SmtpState.CONNECT == smtpState) {
            return new Response(220,
                "SMTP service ready",
                SmtpState.GREET);
        }
        else {
            return new Response(503,
                "Bad sequence of commands: " + this,
                smtpState);
        }
    }

}
