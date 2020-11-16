package lviv.javaclub.order.saga.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCompletedAction implements Action<String, String> {

    @Override
    public void execute(StateContext<String, String> context) {
        log.info("completed: [{}]", context.getExtendedState().getVariables().get("orderId"));
    }

}
