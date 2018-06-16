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

/**
 * Container for a anonymised SMTP message - headers stripped and message body removed.
 */
public class AnonymisedMailMessageImpl extends MailMessageImpl
{
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
            switch(name)
            {
                case("From"):               From        = new AnonymisedMailAddress(value); break;
                case("To"):                 To          = new AnonymisedMailAddress(value); break;
                case("Date"):               Date        = value;                            break;
                case("X-MS-Has-Attach"): hasAttachments = value.contains("yes");            break;
            }
        }
        catch (Exception e)
        {
            /* todo: log it */
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
        return  this.From.toString()             + csvSeparator +
                this.To.toString()               + csvSeparator +
                this.Date                        + csvSeparator +
                this.hasAttachments.toString()   + lineSeparator;
    }
}
