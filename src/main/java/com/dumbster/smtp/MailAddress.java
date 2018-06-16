package com.dumbster.smtp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailAddress
{
    private String localPart;
    private String globalPart;

    Pattern mailAddressPattern = Pattern.compile("([a-z0-9.\\-_]+)@([a-z.]+)");
    MailAddress(String localPart, String globalPart)
    {
        this.localPart = localPart;
        this.globalPart = globalPart;
    }

    MailAddress(String raw) throws Exception
    {
        final Matcher matcher = mailAddressPattern.matcher(raw);
        if(!matcher.matches()) throw new Exception("No valid mail!");
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