package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceTest {
    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @MockBean
    private EmailClient mockClient;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @Test
    public void sendEmailForSubmissionConfirmationShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendEmail(
            EMAIL_ADDRESS,
            EmailTemplateNames.APPLIC_SUBMISSION.name(),
            null,
            LanguagePreference.ENGLISH);

        verify(mockClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(LanguagePreference.ENGLISH).get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplatesConfig.getTemplateVars().get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            anyString());
    }

    @Test
    public void sendWelshEmailForSubmissionConfirmationShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendEmail(
            EMAIL_ADDRESS,
            EmailTemplateNames.APPLIC_SUBMISSION.name(),
            null,
            LanguagePreference.WELSH
        );

        verify(mockClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(LanguagePreference.WELSH).get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplatesConfig.getTemplateVars().get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            anyString()
        );
    }

    @Test
    public void useConfigTemplateVarsWhenAvailable()
        throws NotificationClientException {
        emailService.sendEmail(EMAIL_ADDRESS,
            EmailTemplateNames.SAVE_DRAFT.name(),
            null,
            LanguagePreference.ENGLISH);

        verify(mockClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(LanguagePreference.ENGLISH).get(EmailTemplateNames.SAVE_DRAFT.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplatesConfig.getTemplateVars().get(EmailTemplateNames.SAVE_DRAFT.name())),
            anyString());
    }

    @Test
    public void useWelshConfigTemplateVarsWhenAvailable()
        throws NotificationClientException {
        emailService.sendEmail(
            EMAIL_ADDRESS,
            EmailTemplateNames.SAVE_DRAFT.name(),
            null,
            LanguagePreference.WELSH
        );

        verify(mockClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(LanguagePreference.WELSH).get(EmailTemplateNames.SAVE_DRAFT.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplatesConfig.getTemplateVars().get(EmailTemplateNames.SAVE_DRAFT.name())),
            anyString());
    }

    @Test
    public void sendEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        //no exception thrown
        emailService.sendEmail(
            EMAIL_ADDRESS,
            EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS,
            LanguagePreference.WELSH
        );
    }

    @Test
    public void sendEmailAndReturnExceptionShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendEmailAndReturnExceptionIfFails(
            EMAIL_ADDRESS,
            EmailTemplateNames.APPLIC_SUBMISSION.name(),
            null,
            "submission notification",
            LanguagePreference.ENGLISH
        );

        verify(mockClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(LanguagePreference.ENGLISH).get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplatesConfig.getTemplateVars().get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            anyString());
    }

    @Test
    public void sendWelshEmailAndReturnExceptionShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendEmailAndReturnExceptionIfFails(EMAIL_ADDRESS,
            EmailTemplateNames.APPLIC_SUBMISSION.name(),
            null,
            "submission notification",
            LanguagePreference.WELSH);

        verify(mockClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(LanguagePreference.WELSH).get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplatesConfig.getTemplateVars().get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            anyString());
    }

    @Test(expected = NotificationClientException.class)
    public void sendEmailShouldAndReturnExceptionPropagateNotificationClientException()
        throws NotificationClientException {
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        emailService.sendEmailAndReturnExceptionIfFails(
            EMAIL_ADDRESS,
            EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name(),
            null,
            "resp does not consent to 2 year separation update notification",
            LanguagePreference.ENGLISH
        );
    }
}
