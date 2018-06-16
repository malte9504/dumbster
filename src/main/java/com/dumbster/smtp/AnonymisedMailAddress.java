package com.dumbster.smtp;

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
        super(anonymise(localPart),globalPart);
    }
    @Override
    protected String convertLocalPart(Matcher m) {
        return anonymise(m.group(1));
    }

    private static String anonymise(String input)
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
        byte[] result = digest.digest(input.getBytes());
        StringBuffer buffer = new StringBuffer();
        for (byte cur: result) 
        {
            buffer.append(Integer.toString((cur & 0xff) + 0x100, 16).substring(1));
        }
         
        return buffer.toString();
    }
}