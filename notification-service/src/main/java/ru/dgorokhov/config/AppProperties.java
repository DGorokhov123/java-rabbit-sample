package ru.dgorokhov.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app")
public class AppProperties {

    private Properties properties = new Properties();
    private Email email = new Email();

    @Getter
    @Setter
    public static class Properties {
        private String notificationFanout = "defaul.fanout";
        private String notificationQueuePostfix = ".queue";
    }

    @Getter
    @Setter
    public static class Email {
        private String provider = "none";          // можно выбрать отправку разными способами: javamail, resend
        private String resendKey = "";             // если используется resend, можно задать ключ
    }

}
