package lviv.javaclub.order.web;

import lviv.javaclub.order.model.Order;
import lviv.javaclub.order.saga.OrderEvent;
import lviv.javaclub.order.saga.statemachine.SagaPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class OrderController {

    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private SagaPersister sagaPersister;

    @GetMapping("/orders")
    public String newOrder(Order order) {
        UUID orderId = UUID.randomUUID();
        order.setOrderId(orderId);

        var stateMachine = stateMachineService.acquireStateMachine(orderId.toString(), true);
        stateMachine.getExtendedState().getVariables().put("orderId", order.getOrderId());
        stateMachine.sendEvent(OrderEvent.PLACE_NEW_ORDER);

        return "done";
    }

}
