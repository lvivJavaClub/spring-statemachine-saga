package lviv.javaclub.order.saga.statemachine;

import lviv.javaclub.order.saga.OrderEvent;
import lviv.javaclub.order.saga.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
public class StatemachineConfiguration {

    @Bean
    public StateMachineService stateMachineService(StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
                                                   SagaPersister sagaPersister) {
        return new DefaultStateMachineService(stateMachineFactory, sagaPersister);
    }

}
