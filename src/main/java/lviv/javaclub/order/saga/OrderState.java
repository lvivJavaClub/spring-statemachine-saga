package lviv.javaclub.order.saga;

public enum OrderState {

    NEW_ORDER,
    CANCELED,
    PAYED,
    IN_PROGRESS,
    JOIN_POINTS,
    FOR_DELIVERY,
    ON_WAY_TO_CUSTOMER,
    DELIVERED,

    AGENT_FLOW,
    AGENT_ASSIGNED,
    AGENT_WAITING_FOR_ORDER,

    KITCHEN_FLOW,
    KITCHEN_COOKING,
    KITCHEN_READY

}
