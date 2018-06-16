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

/**
 * Container for a anonymised SMTP message - headers stripped and message body removed.
 */
public class AnonymisedMailMessageImpl extends MailMessageImpl
{

    private final Logger LOG = LoggerFactory.getLogger(AnonymisedMailMessageImpl.class);
    private MailAddress From;
    private MailAddress To;
    private String Date;
    private Boolean hasAttachments = false;

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
                 if(name.startsWith("From")){            From    = new AnonymisedMailAddress(value); }
            else if(name.startsWith("To")){              To      = new AnonymisedMailAddress(value); }
            else if(name.startsWith("Date")){            Date    = value; }
            else if(name.startsWith("X-MS-Has-Attach")){ hasAttachments = value.contains("yes"); }
            else { }
        }
        catch (Exception e)
        {
            LOG.warn("Error parsing mail address: ",e);
        }
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
            return this.From.toString() + csvSeparator +
                    this.To.toString() + csvSeparator +
                    this.Date + csvSeparator +
                    this.hasAttachments.toString() + lineSeparator;
        }
        catch (NullPointerException e)
        {
            LOG.warn("Incomplete Mail:",e);
            return "";
        }
    }
}
