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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientSession implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger(ClientSession.class);

    private IOSource socket;
    private volatile MailStore mailStore;
    private MailMessage msg;
    private Response smtpResponse;
    private PrintWriter out;
    private BufferedReader input;
    private SmtpState smtpState;
    private String line;
    private String lastHeaderName = null;
    private volatile boolean running = true;


    protected ClientSession(IOSource socket, MailStore mailStore)
    {
        this.socket = socket;
        this.mailStore = mailStore;
        this.msg = new AnonymisedMailMessageImpl();
        Request request = Request.initialRequest();
        smtpResponse = request.execute(this.mailStore, msg);
    }

    @Override
    public void run()
    {
        do {
            try {
                prepareSessionLoop();
                sessionLoop();
                running = false;
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
            catch (IOException e) {
                LOG.warn("Caught IO Exception", e);
            }
        }
        while (running);

        try {
            socket.close();
        }
        catch (Exception e) {
            LOG.debug("While closing socket", e);
        }
    }

    public void stop()
    {
        this.running = false;
    }

    protected void doWaitInResponse() throws InterruptedException
    {
        // GNDN
    }

    protected boolean prepareSessionLoop() throws IOException, InterruptedException
    {
        prepareOutput();
        prepareInput();
        sendResponse();
        updateSmtpState();
        return true;
    }

    private void prepareOutput() throws IOException
    {
        out = socket.getOutputStream();
        out.flush();
    }

    private void prepareInput() throws IOException
    {
        input = socket.getInputStream();
    }

    private void sendResponse() throws InterruptedException
    {
        if (smtpResponse.getCode() > 0) {
            doWaitInResponse();
            int code = smtpResponse.getCode();
            String message = smtpResponse.getMessage();
            out.print(code + " " + message + "\r\n");
            out.flush();
        }
    }

    private void updateSmtpState()
    {
        smtpState = smtpResponse.getNextState();
    }

    protected void sessionLoop() throws IOException, InterruptedException
    {
        while (smtpState != SmtpState.CONNECT && readNextLineReady()) {
            Request request = Request.createRequest(smtpState, line);
            smtpResponse = request.execute(mailStore, msg);
            storeInputInMessage(request);
            sendResponse();
            updateSmtpState();
            saveAndRefreshMessageIfComplete();
        }
    }

    private boolean readNextLineReady() throws IOException
    {
        readLine();
        return line != null;
    }

    private void readLine() throws IOException
    {
        line = input.readLine();
    }

    private void saveAndRefreshMessageIfComplete()
    {
        if (smtpState == SmtpState.QUIT) {
            mailStore.addMessage(msg);
            msg = new AnonymisedMailMessageImpl();
        }
    }

    private void storeInputInMessage(Request request)
    {
        String params = request.getParams();
        if (null == params) {
            return;
        }

        if (SmtpState.DATA_HDR.equals(smtpResponse.getNextState())) {
            addDataHeader(params);
            return;
        }

        if (SmtpState.DATA_BODY == smtpResponse.getNextState()) {
            msg.appendBody(params);
            return;
        }
    }

    private void addDataHeader(String params)
    {
        int headerNameEnd = params.indexOf(':');
        if (headerNameEnd > 0 && !whiteSpacedLineStart(params)) {
            lastHeaderName = params.substring(0, headerNameEnd).trim();
            String value = params.substring(headerNameEnd + 1).trim();
            msg.addHeader(lastHeaderName, value);
        }
        else if (whiteSpacedLineStart(params) && lastHeaderName != null) {
            msg.appendHeader(lastHeaderName, params);
        }
    }

    private boolean whiteSpacedLineStart(String s)
    {
        if (s == null || "".equals(s)) {
            return false;
        }
        char c = s.charAt(0);
        return c == 32 || c == 0x0b || c == '\n' ||
               c == '\r' || c == '\t' || c == '\f';
    }

}
