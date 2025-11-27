package ru.otus.hw.services;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.dto.Bread;
import ru.otus.hw.dto.Order;

import java.util.List;

@MessagingGateway
public interface StoreGateway {

    @Gateway(requestChannel = "orderChannel", replyChannel = "breadDeliveryChannel")
    List<Bread> placeOrder(Order order);
}