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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public abstract class AbstractSmtpServerTest
{
    private SmtpServer server;
    private int smtpPort;

    private static final String SERVER = "localhost";
    private static final String FROM = "sender@here.com";
    private static final String TO = "receiver@there.com";
    private static final String SUBJECT = "Test";
    private static final String BODY = "Test Body";
    private static final String FILENAME = "LICENSE.txt";

    private final int WAIT_TICKS = 10000;

    @Before
    public void setup()
    {
        server = getSmtpServer();
        smtpPort = server.getPort();
    }

    protected abstract SmtpServer getSmtpServer();

    @After
    public void teardown() throws InterruptedException
    {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testNoMessageSentButWaitingDoesNotHang()
    {
        server.anticipateMessageCountFor(1, 10);
        assertEquals(0, server.getEmailCount());
    }

    @Test
    public void testSend() throws Exception
    {
        sendMessage(smtpPort, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 1);
        MailMessage email = server.getMessage(0);
        //assertEquals("Test", email.getFirstHeaderValue("Subject"));
        //assertEquals("Test Body", email.getBody());
    }

    @Test
    public void testClearMessages() throws Exception
    {
        sendMessage(smtpPort, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 1);
        sendMessage(smtpPort, FROM, SUBJECT, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 2);
        server.clearMessages();
        assertTrue(server.getEmailCount() == 0);
    }

    @Test
    public void testSendWithLongSubject() throws Exception
    {
        String longSubject = StringUtil.longString(500);
        sendMessage(smtpPort, FROM, longSubject, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertTrue(server.getEmailCount() == 1);
        MailMessage email = server.getMessage(0);
        //assertEquals(longSubject, email.getFirstHeaderValue("Subject"));
        //assertEquals(500, longSubject.length());
        //assertEquals("Test Body", email.getBody());
    }

    @Test
    public void testSendWithFoldedSubject() throws Exception
    {
        String subject = "This\r\n is a folded\r\n Subject line.";
        MailMessage email = sendMessageWithSubject(subject);
        //assertEquals("This is a folded Subject line.", email.getFirstHeaderValue("Subject"));
    }

    private MailMessage sendMessageWithSubject(String subject) throws Exception
    {
        sendMessage(smtpPort, FROM, subject, BODY, TO);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        assertEquals(1, server.getEmailCount());
        return server.getMessage(0);
    }

    @Test
    public void testSendWithFoldedSubjectLooksLikeHeader() throws Exception
    {
        String subject = "This\r\n really: looks\r\n strange.";
        MailMessage email = sendMessageWithSubject(subject);
        //assertEquals("This really: looks strange.", email.getFirstHeaderValue("Subject"));
    }

    @Test
    @Ignore
    // should this work?
    public void testSendMessageWithCarriageReturn() throws Exception
    {
        String bodyWithCR = "\r\nKeep these pesky carriage returns\r\n";
        sendMessage(smtpPort, FROM, SUBJECT, bodyWithCR, TO);
        assertEquals(1, server.getEmailCount());
        MailMessage email = server.getMessage(0);
        //assertEquals(bodyWithCR, email.getBody());
    }

    @Test
    public void testSendTwoMessagesSameConnection() throws Exception
    {
        MimeMessage[] mimeMessages = new MimeMessage[2];
        Properties mailProps = getMailProperties(smtpPort);
        Session session = Session.getInstance(mailProps, null);

        mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
        mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

        Transport transport = session.getTransport("smtp");
        transport.connect("localhost", smtpPort, null, null);

        for (MimeMessage mimeMessage : mimeMessages) {
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }

        transport.close();

        server.anticipateMessageCountFor(2, WAIT_TICKS);
        assertEquals(2, server.getEmailCount());
    }

    @Test
    public void testSendingFileAttachment() throws MessagingException
    {
        Properties props = getMailProperties(smtpPort);
        Session session = Session.getInstance(props, null);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(FROM));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
        message.setSubject(SUBJECT);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(buildMessageBody());
        multipart.addBodyPart(buildFileAttachment());
        message.setContent(multipart);
        Transport.send(message);
        server.anticipateMessageCountFor(1, WAIT_TICKS);
        //assertTrue(server.getMessage(0).getBody().indexOf("Apache License") > 0);
    }

    private MimeBodyPart buildFileAttachment() throws MessagingException
    {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new javax.activation.FileDataSource(FILENAME);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(FILENAME);
        return messageBodyPart;
    }

    private MimeBodyPart buildMessageBody() throws MessagingException
    {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(BODY);
        return messageBodyPart;
    }

    @Test
    public void testSendTwoMsgsWithLogin() throws Exception
    {
        Properties props = System.getProperties();

        Session session = Session.getInstance(props, null);
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(FROM));

        InternetAddress.parse(TO, false);
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO, false));
        msg.setSubject(SUBJECT);

        msg.setText(BODY);
        msg.setHeader("X-Mailer", "musala");
        msg.setSentDate(new Date());
        msg.saveChanges();

        Transport transport = null;

        try {
            transport = session.getTransport("smtp");
            transport.connect(SERVER, smtpPort, "ddd", "ddd");
            transport.sendMessage(msg, InternetAddress.parse(TO, false));
            transport.sendMessage(msg, InternetAddress.parse("dimiter.bakardjiev@musala.com", false));
        }
        finally {
            if (transport != null) {
                transport.close();
            }
        }

        server.anticipateMessageCountFor(2, WAIT_TICKS);
        assertEquals(2, server.getEmailCount());
        MailMessage email = server.getMessage(0);
        //assertEquals("Test", email.getFirstHeaderValue("Subject"));
        //assertEquals("Test Body", email.getBody());
    }

    private Properties getMailProperties(int port)
    {
        Properties mailProps = new Properties();
        mailProps.setProperty("mail.smtp.host", "localhost");
        mailProps.setProperty("mail.smtp.port", Integer.toString(port));
        mailProps.setProperty("mail.smtp.sendpartial", "true");
        return mailProps;
    }

    private void sendMessage(int port, String from, String subject, String body, String to) throws MessagingException
    {
        Properties mailProps = getMailProperties(port);
        Session session = Session.getInstance(mailProps, null);

        MimeMessage msg = createMessage(session, from, to, subject, body);
        Transport.send(msg);
    }

    private MimeMessage createMessage(Session session, String from, String to, String subject, String body) throws MessagingException
    {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(body);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return msg;
    }
}
