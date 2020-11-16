package lviv.javaclub.order.saga.listener;

import lombok.RequiredArgsConstructor;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import lviv.javaclub.order.saga.OrderEvent;
import lviv.javaclub.order.saga.OrderState;
import org.springframework.context.event.EventListener;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentEventsListener {

    private final StateMachineService<OrderState, OrderEvent> stateMachineService;

    @EventListener(condition = "#e.source.equals('PAYMENT') && #e.action.equals('withdrawn')")
    public void onOrderPayed(ExternalOrderEvent e) {
        String machineId = e.getOrderId().toString();
        StateMachine<OrderState, OrderEvent> orderSaga = stateMachineService.acquireStateMachine(machineId);
        orderSaga.sendEvent(OrderEvent.ORDER_PAYED);
    }

}
