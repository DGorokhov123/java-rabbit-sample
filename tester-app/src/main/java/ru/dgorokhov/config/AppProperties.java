package ru.dgorokhov.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("app")
public class AppProperties {

    private Properties properties = new Properties();

    private String mailUrl = "http://localhost:8025";

    @Getter
    @Setter
    public static class Properties {
        private String notificationFanout = "defaul.fanout";
        private String notificationQueuePostfix = ".queue";
    }

}
