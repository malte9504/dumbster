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
package com.dumbster.smtp.eml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EMLMailMessageTest
{

    private EMLMailMessage message;
    /*
     * Example From http://en.wikipedia.org/wiki/Simple_Mail_Transfer_Protocol
     */
    private final String text =
                              "From: \"Bob Example\" <bob@example.org>\n" +
                                "To: \"Alice Example\" <alice@example.com>\n" +
                                "Cc: theboss@example.com\n" +
                                "Date: Tue, 15 January 2008 16:02:43 -0500\n" +
                                "Subject: Test message\n" +
                                "\n" +
                                "Hello Alice.\n" +
                                "This is a test message with 5 header fields and 4 lines in the message body.\n" +
                                "Your friend,\n" +
                                "Bob\n";

    @Before
    public void setup()
    {
        message = new EMLMailMessage(new ByteArrayInputStream(text.getBytes()));
    }

    @Test
    public void testReadHeaders()
    {
        String[] from = message.getHeaderValues("From");
        assertEquals(1, from.length);
        assertEquals("\"Bob Example\" <bob@example.org>", from[0]);
        assertEquals(1, message.getHeaderValues("To").length);
    }

    @Test
    public void testGetBody()
    {
        assertEquals("Hello Alice.\n" +
                     "This is a test message with 5 header fields and 4 lines in the message body.\n" +
                     "Your friend,\n" +
                     "Bob",
            message.getBody());
    }

    @Test
    public void testGetHeaderNames()
    {
        List<String> headers = new ArrayList<String>();
        Iterator<String> iterator = message.getHeaderNames();
        while (iterator.hasNext()) {
            headers.add(iterator.next());
        }
        assertEquals(5, headers.size());
    }

    @Test
    public void testGetFirstHeaderValue()
    {
        String firstFrom = message.getFirstHeaderValue("From");
        assertEquals("\"Bob Example\" <bob@example.org>", firstFrom);
        assertNull(message.getFirstHeaderValue("MissingHeader"));
    }
}
