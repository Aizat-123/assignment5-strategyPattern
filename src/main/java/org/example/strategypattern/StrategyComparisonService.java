package org.example.strategypattern;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StrategyComparisonService {
    public Map<String, RouteDecision> compare(List<RouteStrategy> strategies,
                                              List<Route> routes,
                                              DeliveryRequest request) {
        if (strategies == null || strategies.isEmpty() || routes == null || request == null) {
            throw new IllegalArgumentException("Strategies, routes and request are required");
        }

        Map<String, RouteDecision> results = new LinkedHashMap<>();
        for (RouteStrategy strategy : strategies) {
            if (strategy == null) {
                throw new IllegalArgumentException("Strategy must not be null");
            }
            String name = strategy.getStrategyName();
            if (name == null || name.isBlank() || results.containsKey(name)) {
                throw new IllegalArgumentException("Strategy names must be non-blank and unique");
            }
            results.put(name, strategy.selectRoute(routes, request));
        }
        return results;
    }
}
