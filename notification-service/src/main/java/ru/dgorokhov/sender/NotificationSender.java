package ru.dgorokhov.sender;

public interface NotificationSender {

    NotificationChannel getChannel();

    void send(NotificationDto notification) throws NotificationDeliveryException;

}
