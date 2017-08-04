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

import static java.util.Objects.requireNonNull;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class RollingMailStore implements MailStore
{
    private final Logger LOG = LoggerFactory.getLogger(RollingMailStore.class);

    private Deque<MailMessage> receivedMail;

    public RollingMailStore()
    {
        this(100);
    }

    public RollingMailStore(int size)
    {
        receivedMail = new LinkedBlockingDeque<MailMessage>(size);
    }

    @Override
    public int getEmailCount()
    {
        return receivedMail.size();
    }

    @Override
    public void addMessage(MailMessage message)
    {
        requireNonNull(message, "message is null");

        LOG.debug("Received message: " + message);

        synchronized (receivedMail) {
            if (!receivedMail.offer(message)) {
                receivedMail.remove();
                receivedMail.add(message);
            }
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
        return getMessages()[index];
    }

    @Override
    public void clearMessages()
    {
        this.receivedMail.clear();
    }
}
