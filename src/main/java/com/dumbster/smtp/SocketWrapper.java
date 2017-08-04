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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketWrapper implements IOSource
{
    private Socket socket;

    public SocketWrapper(Socket socket) throws IOException
    {
        this.socket = socket;
        this.socket.setSoTimeout(10000); // protects against hanged clients
    }

    @Override
    public BufferedReader getInputStream() throws IOException
    {
        return new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
    }

    @Override
    public PrintWriter getOutputStream() throws IOException
    {
        return new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));
    }

    @Override
    public void close() throws IOException
    {
        socket.close();
    }

}
