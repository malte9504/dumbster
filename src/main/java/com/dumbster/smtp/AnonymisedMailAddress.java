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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;

public class AnonymisedMailAddress extends MailAddress
{
    AnonymisedMailAddress(String raw) throws Exception
    {
        super(raw);
    }
    AnonymisedMailAddress(String localPart, String globalPart)
    {
        super(anonymize(localPart),globalPart);
    }
    @Override
    protected String convertLocalPart(Matcher m) {
        return anonymize(m.group(1));
    }

    protected static String anonymize(String input)
    {
        String res = sha1(input);
        if(res == null)
        {
            res= input.charAt(0)+"######";
        }
        else
        {
            res = res.substring(0, 7);
        }
        return res;
    }

    private static String sha1(String input)
    {
        MessageDigest digest = null;
        try{
            digest = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
        byte[] result = digest.digest(input.getBytes(StandardCharsets.ISO_8859_1));
        StringBuffer buffer = new StringBuffer();
        for (byte cur: result)
        {
            buffer.append(Integer.toString((cur & 0xff) + 0x100, 16).substring(1));
        }

        return buffer.toString();
    }
}
