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

import java.util.Iterator;

public interface MailMessage
{

    /**
     * Gets an iterator over header names.
     *
     * @return {@code Iterator}
     */
    Iterator<String> getHeaderNames();

    /**
     * Gets the values of a given header.
     *
     * @param name
     *        the name of the header.
     * @return the list of values associated with this header.
     */
    String[] getHeaderValues(String name);

    /**
     * A shortcut to get only the first value of a header
     *
     * @param name
     *        is name of the header
     * @return the first value of the header.
     */
    String getFirstHeaderValue(String name);

    /**
     * Returns the body of the message.
     *
     * @return the body of the message.
     */
    String getBody();

    /**
     * Adds a header to the message
     *
     * @param name
     *        is the name of the header.
     * @param value
     *        is the value to add to the header.
     */
    void addHeader(String name, String value);

    /**
     * Append some text to the last existing value of a header.
     * It differs from {@code addHeader} method because it doesn't add a new header entry,
     * instead it appends the given value to an existing header entry.
     * If the given header name doesn't exist this method will add a new header.
     *
     * @param name
     *        is the name of the header
     * @param value
     *        is the value to append to the header.
     */
    void appendHeader(String name, String value);

    /**
     * Appends the given text to the body.
     * The text will be added in a new line.
     *
     * @param line
     *        is the text to append.
     */
    void appendBody(String line);

}
