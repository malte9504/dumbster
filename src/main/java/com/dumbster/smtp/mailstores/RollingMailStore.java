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
package com.dumbster.smtp.mailstores;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RollingMailStore implements MailStore
{

    private List<MailMessage> receivedMail;

    public RollingMailStore()
    {
        receivedMail = Collections.synchronizedList(new ArrayList<MailMessage>());
    }

    @Override
    public int getEmailCount()
    {
        return receivedMail.size();
    }

    @Override
    public void addMessage(MailMessage message)
    {
        System.out.println("\n\nReceived message:\n" + message);
        receivedMail.add(message);
        if (getEmailCount() > 100) {
            receivedMail.remove(0);
        }
    }

    @Override
    public MailMessage[] getMessages()
    {
        return receivedMail.toArray(new MailMessage[receivedMail.size()]);
    }

    @Override
    public MailMessage getMessage(int index)
    {
        return receivedMail.get(index);
    }

    @Override
    public void clearMessages()
    {
        this.receivedMail.clear();
    }
}
