package com.dumbster.smtp.action;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;
import com.dumbster.smtp.Response;
import com.dumbster.smtp.SmtpState;

public interface Action
{

    @Override
    public abstract String toString();

    public abstract Response response(SmtpState smtpState, MailStore mailStore, MailMessage currentMessage);

}
