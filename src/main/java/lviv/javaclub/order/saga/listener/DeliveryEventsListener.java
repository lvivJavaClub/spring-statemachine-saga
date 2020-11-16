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
public class DeliveryEventsListener {

    private final StateMachineService<OrderState, OrderEvent> stateMachineService;

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('AGENT_ASSIGNED')")
    public void onAgentAssigned(ExternalOrderEvent e) {
        String machineId = e.getOrderId().toString();
        StateMachine<OrderState, OrderEvent> orderSaga = stateMachineService.acquireStateMachine(machineId);
        orderSaga.sendEvent(OrderEvent.AGENT_ASSIGNED);
    }

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('ARRIVED')")
    public void onAgentArrived(ExternalOrderEvent e) {
        String machineId = e.getOrderId().toString();
        StateMachine<OrderState, OrderEvent> orderSaga = stateMachineService.acquireStateMachine(machineId);
        orderSaga.sendEvent(OrderEvent.AGENT_ARRIVED_TO_KITCHEN);
    }

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('OUT_OF_WAY')")
    public void onAgentOnWayToCustomer(ExternalOrderEvent e) {
        String machineId = e.getOrderId().toString();
        StateMachine<OrderState, OrderEvent> orderSaga = stateMachineService.acquireStateMachine(machineId);
        orderSaga.sendEvent(OrderEvent.AGENT_OUT_FOR_DELIVERY);
    }

    @EventListener(condition = "#e.source.equals('DELIVERY') && #e.action.equals('ORDER_DELIVERED')")
    public void onAgentDeliveredOrder(ExternalOrderEvent e) {
        String machineId = e.getOrderId().toString();
        StateMachine<OrderState, OrderEvent> orderSaga = stateMachineService.acquireStateMachine(machineId);
        orderSaga.sendEvent(OrderEvent.ORDER_DELIVERED);
    }

}
