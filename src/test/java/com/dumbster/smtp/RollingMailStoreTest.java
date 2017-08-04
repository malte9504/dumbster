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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.dumbster.smtp.mailstores.RollingMailStore;
import org.junit.Before;
import org.junit.Test;

public class RollingMailStoreTest
{

    private MailStore mailStore;

    @Before
    public void setup()
    {
        mailStore = new RollingMailStore();
    }

    @Test
    public void testNewMailStoreHasNoMail()
    {
        assertEquals(0, mailStore.getEmailCount());
    }

    @Test
    public void testAddOneMessageLeavesOneMail()
    {
        addAMessage();
        assertEquals(1, mailStore.getEmailCount());
    }

    private void addAMessage()
    {
        MailMessage message = new MailMessageImpl();
        mailStore.addMessage(message);
    }

    @Test
    public void testNewMailStoreHasEmptyMailList()
    {
        assertEquals(0, mailStore.getMessages().length);
    }

    @Test
    public void testAddOneMessageLeavesOneMailInMailMessagesArray()
    {
        addAMessage();
        assertEquals(1, mailStore.getMessages().length);
    }

    @Test
    public void testGettingMailFromEmptyMailStoreThrowsIndexOutOfBounds()
    {
        try {
            mailStore.getMessage(0);
            fail("Should have raised exception.");
        }
        catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void testGettingMail0FromMailStoreWithAnItemWorks()
    {
        addAMessage();
        assertNotNull(mailStore.getMessage(0));
    }

    @Test
    public void testMailRollsOff()
    {
        MailMessage firstMessage = new MailMessageImpl();
        firstMessage.appendBody("First Post!");
        mailStore.addMessage(firstMessage);

        assertEquals("First Post!", mailStore.getMessage(0).getBody());
        for (int i = 0; i < 100; i++) {
            addAMessage();
        }

        assertEquals(100, mailStore.getEmailCount());
        assertEquals("", mailStore.getMessage(0).getBody());
    }
}
