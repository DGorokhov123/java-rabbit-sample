package ru.dgorokhov.sender;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.dgorokhov.config.AppProperties;

/**
 * Имплементация NotificationSender
 * Реализует отправку уведомлений на email
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final AppProperties appProperties;
    private final JavaMailSender mailSender;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationDto notification) throws NotificationDeliveryException {
        String provider = appProperties.getEmail().getProvider();
        if (provider.equalsIgnoreCase("resend")) {
            sendByResend(notification);
        } else if (provider.equalsIgnoreCase("javamail")) {
            sendByJavaMail(notification);
        }
    }

    /*
    Можно отправлять через сервис, например resend. сейчас только так и можно отправлять из-за спам-фильтров
     */
    private void sendByResend(NotificationDto notification) throws NotificationDeliveryException {
        String email = notification.getChannelData().get(NotificationChannel.EMAIL);
        Resend resend = new Resend(appProperties.getEmail().getResendKey());

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("UserService <administration@dgorokhov.ru>")
                .to(email)
                .subject(notification.getTitle())
                .html(notification.getBody())
                .build();

        try {
            resend.emails().send(params);
            log.debug("Sent email notification to {}", email);
        } catch (ResendException e) {
            throw new NotificationDeliveryException(e.getMessage());
        }
    }

    /*
    отправка через javamail
    типовая реализация, удобно тестировать в связке с mailpit
    в проде такое лучше не использовать
     */
    private void sendByJavaMail(NotificationDto notification) throws NotificationDeliveryException {
        String email = notification.getChannelData().get(NotificationChannel.EMAIL);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("UserService <administration@dgorokhov.ru>");
        message.setTo(email);
        message.setSubject(notification.getTitle());
        message.setText(notification.getBody());
        try {
            mailSender.send(message);
            log.debug("Sent email notification to {}", email);
        } catch (MailException e) {
            throw new NotificationDeliveryException(e.getMessage());
        }
    }

}
