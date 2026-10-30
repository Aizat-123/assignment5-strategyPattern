package org.example.strategypattern;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Location warehouse = new Location("Warehouse", 44.85, 65.50);
        Location customer = new Location("Customer", 44.87, 65.53);

        // Fields after destination: distance, time, base cost, emission, traffic, available.
        List<Route> routes = List.of(
                new Route("Fast", warehouse, customer, 30, 10, 1500, 3, 0, true),
                new Route("Cheap", warehouse, customer, 20, 40, 0, 4, 0, true),
                new Route("Eco", warehouse, customer, 10, 35, 1500, 0.2, 0, true),
                new Route("Balanced", warehouse, customer, 15, 20, 750, 1, 0, true),
                new Route("Traffic", warehouse, customer, 12, 8, 1000, 2, 30, true),
                new Route("Unavailable", warehouse, customer, 5, 5, 100, 0.1, 0, false),
                new Route("OverBudget", warehouse, customer, 10, 15, 6000, 1, 0, true),
                new Route("Late", warehouse, customer, 10, 70, 500, 0.5, 5, true)
        );
        DeliveryRequest request = new DeliveryRequest(
                DeliveryPriority.STANDARD, 5000, 60, false
        );

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        printDecision(planner, routes, request);

        // Switch 1: fastest -> cheapest.
        planner.setStrategy(new CheapestRouteStrategy());
        printDecision(planner, routes, request);

        // Switch 2: cheapest -> eco friendly.
        planner.setStrategy(new EcoFriendlyRouteStrategy());
        printDecision(planner, routes, request);

        // Switch 3: eco friendly -> balanced.
        planner.setStrategy(new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2));
        printDecision(planner, routes, request);
        StrategySelector selector = new StrategySelector();
        selector.register(DeliveryPriority.URGENT, new FastestRouteStrategy());
        selector.register(DeliveryPriority.ECONOMY, new CheapestRouteStrategy());
        selector.register(DeliveryPriority.ECO, new EcoFriendlyRouteStrategy());
        selector.register(DeliveryPriority.STANDARD, new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2));

        System.out.println("AUTOMATIC STRATEGY SELECTION");
        for (DeliveryPriority priority : DeliveryPriority.values()) {
            DeliveryRequest automaticRequest = new DeliveryRequest(priority, 5000, 60, false);
            planner.setStrategy(selector.selectStrategy(automaticRequest));
            System.out.println("Priority: " + priority);
            printDecision(planner, routes, automaticRequest);
        }
    }

    private static void printDecision(RoutePlanner planner, List<Route> routes,
                                      DeliveryRequest request) {
        RouteDecision decision = planner.plan(routes, request);
        System.out.println(planner.getStrategyName());
        System.out.println(decision.explanation());
        for (String reason : decision.rejectedRoutes()) {
            System.out.println("Rejected: " + reason);
        }
        System.out.println();
    }
}
