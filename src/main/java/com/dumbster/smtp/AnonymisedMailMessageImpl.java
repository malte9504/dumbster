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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Container for a anonymised SMTP message - headers stripped and message body removed.
 */
public class AnonymisedMailMessageImpl extends MailMessageImpl
{

    private final Logger LOG = LoggerFactory.getLogger(AnonymisedMailMessageImpl.class);
    private MailAddress From;
    private List<MailAddress> To = new ArrayList<>(1);
    private String Date = "";
    private String Attachments = "";

    private final Pattern filenamePattern   = Pattern.compile("\\sname=\"{0,1}(([a-zA-Z0-9.-_-\\s]+)(\\.[a-zA-Z0-9]+))");
    private final Pattern sizePattern       = Pattern.compile("\\ssize=\\\"{0,1}([0-9]+)");
    public AnonymisedMailMessageImpl()
    {
        super();
    }

    @Override
    public String getBody()
    {
        return "---removed---";
    }

    @Override
    public void addHeader(String name, String value)
    {
        try{
            if      (name.startsWith("From")){
                From    = new AnonymisedMailAddress(value);
            }
            else if (name.startsWith("To") || name.startsWith("Cc"))
            {
                 for (String address: value.split(";")) To.add(new AnonymisedMailAddress(address));
             }
            else if (name.startsWith("Date")){
                Date    = value;
             }
//            else if (name.startsWith("X-MS-Has-Attach") && value.contains("yes")) {
//                 Attachments = "List: ";
//             }
            else {
                appendBody(value);
            }
        }
        catch (Exception e)
        {
            LOG.warn("Error parsing mail address: ",e);
        }
    }

    @Override
    public void appendBody(String line) {
        Matcher filename = filenamePattern.matcher(line);
        if(filename.find()) Attachments += AnonymisedMailAddress.anonymize(filename.group(2))+filename.group(3)+" ";
        Matcher filesize = sizePattern.matcher("line");
        if(filename.find()) Attachments += " ("+filesize.group(1)+" Bytes)";
    }

    @Override
    public void appendHeader(String name, String value)
    {
       addHeader(name, value);
    }

    private char csvSeparator  = ';';
    private char lineSeparator = '\n';
    @Override
    public String toString()
    {
        try {
            StringBuffer buf = new StringBuffer();
            buf.append(this.Date                + csvSeparator);
            buf.append(this.From.toString()     + csvSeparator);
            buf.append(this.Attachments         + csvSeparator);
            for(MailAddress address: To)
                buf.append(address.toString()   + csvSeparator);
            buf.append(lineSeparator);
            return buf.toString();
        }
        catch (NullPointerException e)
        {
            LOG.warn("Incomplete Mail:",e);
            return "";
        }
    }
}
