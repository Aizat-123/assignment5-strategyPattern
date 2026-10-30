package org.example.strategypattern;

import java.util.EnumMap;
import java.util.Map;

public class StrategySelector {
    private final Map<DeliveryPriority, RouteStrategy> strategies =
            new EnumMap<>(DeliveryPriority.class);

    public void register(DeliveryPriority priority, RouteStrategy strategy) {
        if (priority == null || strategy == null) {
            throw new IllegalArgumentException("Priority and strategy are required");
        }
        strategies.put(priority, strategy);
    }

    public RouteStrategy selectStrategy(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        RouteStrategy strategy = strategies.get(request.priority());
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for " + request.priority());
        }
        return strategy;
    }
}
