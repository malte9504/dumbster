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


public class List implements Action
{

    //private Integer messageIndex = null;

    public List(String params)
    {
        try {
            Integer messageIndex = Integer.valueOf(params);
            if (messageIndex > -1) {
                //this.messageIndex = messageIndex;
            }
        }
        catch (NumberFormatException ignored) {
        }
    }

    @Override
    public String toString()
    {
        return "LIST";
    }

    @Override
    public Response response(SmtpState smtpState, MailStore mailStore, MailMessage currentMessage)
    {
        return new Response(252, "Not supported", smtpState);
        /*
        StringBuffer result = new StringBuffer();
        if (messageIndex != null && messageIndex < mailStore.getEmailCount()) {
            result.append("\n-------------------------------------------\n");
            result.append(mailStore.getMessage(messageIndex).toString());
        }
        result.append("There are ");
        result.append(mailStore.getEmailCount());
        result.append(" message(s).");
        return new Response(250, result.toString(), SmtpState.GREET);
        */
    }
}
