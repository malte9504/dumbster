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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailAddress
{
    private String localPart;
    private String globalPart;

    Pattern mailAddressPattern = Pattern.compile("([a-zA-Z0-9äöüÄÖÜ.\\-_]+)\\@([a-zA-Z0-9äöüÄÖÜ.\\-_]+\\.[a-zA-Z]+)",Pattern.CASE_INSENSITIVE);
    MailAddress(String localPart, String globalPart)
    {
        this.localPart = localPart;
        this.globalPart = globalPart;
    }

    MailAddress(String raw) throws Exception
    {
        final Matcher matcher = mailAddressPattern.matcher(raw);
        if(!matcher.find())
        {
            throw new Exception("Invalid mail! ("+raw+")");
        }
        this.localPart = convertLocalPart(matcher);
        this.globalPart = convertGlobalPart(matcher);
    }
    protected String convertLocalPart(Matcher m)
    {
        return m.group(1);
    }

    protected String convertGlobalPart(Matcher m)
    {
        return m.group(2);
    }

    @Override
    public String toString() {
        return localPart+'@'+globalPart;
    }
}
