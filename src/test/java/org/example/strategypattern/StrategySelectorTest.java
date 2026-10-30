package org.example.strategypattern;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StrategySelectorTest {
    @Test
    void selectsRegisteredStrategiesForAllPriorities() {
        StrategySelector selector = new StrategySelector();
        RouteStrategy[] strategies = {new FastestRouteStrategy(), new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(), new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2)};
        DeliveryPriority[] priorities = DeliveryPriority.values();
        for (int i = 0; i < priorities.length; i++) {
            selector.register(priorities[i], strategies[i]);
            assertSame(strategies[i], selector.selectStrategy(
                    new DeliveryRequest(priorities[i], 5000, 60, false)));
        }
    }

    @Test
    void registrationCanBeReplacedWithoutChangingSelector() {
        StrategySelector selector = new StrategySelector();
        selector.register(DeliveryPriority.STANDARD, new FastestRouteStrategy());
        RouteStrategy replacement = new CheapestRouteStrategy();
        selector.register(DeliveryPriority.STANDARD, replacement);
        assertSame(replacement, selector.selectStrategy(
                new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 60, false)));
    }

    @Test
    void missingRegistrationIsExplicitFailure() {
        assertThrows(IllegalArgumentException.class, () -> new StrategySelector().selectStrategy(
                new DeliveryRequest(DeliveryPriority.ECO, 5000, 60, true)));
    }

    @Test
    void rejectsMissingInputs() {
        StrategySelector selector = new StrategySelector();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> selector.selectStrategy(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> selector.register(null, new FastestRouteStrategy())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> selector.register(DeliveryPriority.URGENT, null))
        );
    }
}
