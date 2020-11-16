package lviv.javaclub.kitchen.service;


import lviv.javaclub.kitchen.model.Order;

import java.util.UUID;

public interface KitchenService {

    void prepareOrderItems(UUID orderId);

    void cancelOrder(UUID orderId);

}
