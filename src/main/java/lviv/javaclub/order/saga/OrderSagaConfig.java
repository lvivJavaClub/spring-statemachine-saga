package lviv.javaclub.order.saga;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lviv.javaclub.integration.model.ExternalOrderEvent;
import lviv.javaclub.order.saga.listener.LogStatesListener;
import lviv.javaclub.order.saga.statemachine.SagaPersister;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.UUID;

import static lviv.javaclub.order.saga.OrderState.AGENT_FLOW;
import static lviv.javaclub.order.saga.OrderState.FOR_DELIVERY;
import static lviv.javaclub.order.saga.OrderState.JOIN_POINTS;
import static lviv.javaclub.order.saga.OrderState.KITCHEN_FLOW;
import static lviv.javaclub.order.saga.OrderState.NEW_ORDER;
import static lviv.javaclub.order.saga.OrderState.PAYED;

@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory(name = OrderSagaConfig.STATEMACHINE_NAME)
@Slf4j
public class OrderSagaConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent>
        implements ApplicationEventPublisherAware {

    public static final String STATEMACHINE_NAME = "orderSaga";

    private final SagaPersister sagaPersister;
    private final LogStatesListener logStatesListener;

    @Setter
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states
                .withStates()
                    .initial(NEW_ORDER)
                    .state(PAYED, ctx -> ctx.getStateMachine().sendEvent(OrderEvent.START_PROCESSING))
                    .fork(OrderState.IN_PROGRESS)
                    .join(OrderState.JOIN_POINTS)
                    .state(OrderState.FOR_DELIVERY,
                            ctx -> {
                                notifyCustomer(ctx, "Delivery is rushing to you");
                                deliveryCommand(ctx, "deliverOrder");
                            })
                    .state(OrderState.ON_WAY_TO_CUSTOMER)
                    .state(OrderState.DELIVERED)
                    .and()

                .withStates()
                    .parent(OrderState.IN_PROGRESS)
                    .initial(OrderState.AGENT_FLOW)
                    .stateDo(OrderState.AGENT_FLOW, ctx -> deliveryCommand(ctx, "assignAgent"))
                    .state(OrderState.AGENT_ASSIGNED)
                    .end(OrderState.AGENT_WAITING_FOR_ORDER)
                    .and()

                .withStates()
                    .parent(OrderState.IN_PROGRESS)
                    .initial(KITCHEN_FLOW)
                    .state(KITCHEN_FLOW, ctx -> kitchenEvent(ctx, "startCooking"))
                    .state(OrderState.KITCHEN_COOKING)
                    .end(OrderState.KITCHEN_READY)
                ;
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                .withInternal()
                    .source(NEW_ORDER)
                    .event(OrderEvent.PLACE_NEW_ORDER)
                    .action(ctx -> paymentEvent(ctx, "CHARGE_CUSTOMER"))
                    .and()
                .withExternal()
                    .source(OrderState.NEW_ORDER).target(OrderState.PAYED)
                    .event(OrderEvent.ORDER_PAYED)
                    .action(ctx -> notifyCustomer(ctx, "Thanks for your order"))
                    .and()
                .withExternal()
                    .source(PAYED).target(OrderState.IN_PROGRESS)
                    .event(OrderEvent.START_PROCESSING)
                    .and()
                .withExternal()
                    .source(JOIN_POINTS).target(FOR_DELIVERY)
                    .and()
                .withExternal()
                    .source(OrderState.FOR_DELIVERY).target(OrderState.ON_WAY_TO_CUSTOMER)
                    .event(OrderEvent.AGENT_OUT_FOR_DELIVERY)
                    .and()
                .withExternal()
                    .source(OrderState.ON_WAY_TO_CUSTOMER).target(OrderState.DELIVERED)
                    .event(OrderEvent.ORDER_DELIVERED)
                    .action(ctx -> notifyCustomer(ctx, "open the dooor"))
                    .and()

                .withFork()
                    .source(OrderState.IN_PROGRESS)
                    .target(AGENT_FLOW)
                    .target(KITCHEN_FLOW)
                    .and()


                // AGENTS FLOW
                .withExternal()
                    .state(OrderState.IN_PROGRESS)
                    .source(OrderState.AGENT_FLOW).target(OrderState.AGENT_ASSIGNED)
                    .event(OrderEvent.AGENT_ASSIGNED)
                    .action(ctx -> notifyCustomer(ctx, "Wait for agent on bike...."))
                    .and()
                .withExternal()
                    .state(OrderState.IN_PROGRESS)
                    .source(OrderState.AGENT_ASSIGNED).target(OrderState.AGENT_WAITING_FOR_ORDER)
                    .event(OrderEvent.AGENT_ARRIVED_TO_KITCHEN)
                    .action(ctx -> notifyCustomer(ctx, "Our delivery is on low start..."))
                    .and()

                // Kitchen Flow
                .withExternal()
                    .state(OrderState.IN_PROGRESS)
                    .source(KITCHEN_FLOW).target(OrderState.KITCHEN_COOKING)
                    .event(OrderEvent.KITCHEN_COOKING)
                    .action(ctx -> notifyCustomer(ctx, "We are cooking for you..."))
                    .and()
                .withExternal()
                    .state(OrderState.IN_PROGRESS)
                    .source(OrderState.KITCHEN_COOKING).target(OrderState.KITCHEN_READY)
                    .event(OrderEvent.KITCHEN_READY)
                    .action(ctx -> notifyCustomer(ctx, "Order is ready, wait for delivery..."))
                    .and()

                .withJoin()
                    .source(OrderState.IN_PROGRESS).target(OrderState.JOIN_POINTS)
        ;
    }

    private void notifyCustomer(StateContext<OrderState, OrderEvent> orderStateOrderEventStateContext, String message) {
        log.info(message);
    }

    private void deliveryCommand(StateContext<OrderState, OrderEvent> ctx, String action) {
        UUID orderId = (UUID) ctx.getExtendedState().getVariables().get("orderId");
        applicationEventPublisher.publishEvent(new ExternalOrderEvent("DELIVERY", action, orderId));
    }

    private void paymentEvent(StateContext<OrderState, OrderEvent> ctx, String action) {
        UUID orderId = (UUID) ctx.getExtendedState().getVariables().get("orderId");
        applicationEventPublisher.publishEvent(new ExternalOrderEvent("PAYMENT", action, orderId));
    }

    private void kitchenEvent(StateContext<OrderState, OrderEvent> ctx, String action) {
        UUID orderId = (UUID) ctx.getExtendedState().getVariables().get("orderId");
        applicationEventPublisher.publishEvent(new ExternalOrderEvent("KITCHEN", action, orderId));
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        config.withConfiguration()
                .machineId(STATEMACHINE_NAME)
                .listener(logStatesListener)
        .and()
            .withPersistence()
                .runtimePersister(sagaPersister);
    }

}
