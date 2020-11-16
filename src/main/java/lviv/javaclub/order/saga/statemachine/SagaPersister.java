package lviv.javaclub.order.saga.statemachine;

import lviv.javaclub.order.saga.OrderEvent;
import lviv.javaclub.order.saga.OrderState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaPersister implements StateMachineRuntimePersister<OrderState, OrderEvent, String> {

    private Map<String, StateMachineContext<OrderState, OrderEvent>> storage = new ConcurrentHashMap<>();

    StateMachineInterceptor interceptor;

    @Override
    public StateMachineInterceptor<OrderState, OrderEvent> getInterceptor() {
        if (interceptor == null) {
            interceptor = new AbstractPersistingStateMachineInterceptor() {

                @Override
                public void write(StateMachineContext context, Object contextObj) {
                    SagaPersister.this.write(context, (String) contextObj);
                }

                @Override
                public StateMachineContext read(Object contextObj) {
                    return SagaPersister.this.read((String) contextObj);
                }
            };
        }
        return interceptor;
    }

    @Override
    public void write(StateMachineContext<OrderState, OrderEvent> context, String contextObj) {
        storage.put(contextObj, context);
    }

    @Override
    public StateMachineContext<OrderState, OrderEvent> read(String contextObj) {
        return storage.get(contextObj);
    }
}
