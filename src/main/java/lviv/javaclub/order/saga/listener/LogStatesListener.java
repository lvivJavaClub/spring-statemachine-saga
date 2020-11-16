package lviv.javaclub.order.saga.listener;

import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.order.saga.OrderEvent;
import lviv.javaclub.order.saga.OrderState;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogStatesListener extends StateMachineListenerAdapter<OrderState, OrderEvent> {


    @Override
    public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
        log.info("state changed [from={},to={}]", from != null ? from.getId() : null, to.getId());
    }

}
