package org.example.strategypattern;

import java.util.ArrayList;
import java.util.List;

public class CheapestRouteStrategy implements RouteStrategy {
    private final RouteConstraintChecker checker = new RouteConstraintChecker();

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> suitableRoutes = checker.filterRoutes(routes, request);

        Route cheapest = suitableRoutes.get(0);
        for (Route route : suitableRoutes) {
            if (RouteMetricsCalculator.calculateCost(route, request)
                    < RouteMetricsCalculator.calculateCost(cheapest, request)) {
                cheapest = route;
            }
        }

        List<String> rejectedRoutes = new ArrayList<>();
        for (Route route : routes) {
            rejectedRoutes.addAll(checker.getRejectionReasons(route, request));
        }

        String explanation = "Selected route: " + cheapest.id()
                + "\nReason: minimum total cost among suitable routes."
                + "\nBase cost: " + cheapest.cost() + " KZT"
                + "\nDistance: " + cheapest.distanceKm() + " km"
                + "\nPriority: " + request.priority()
                + "\nTotal cost: " + RouteMetricsCalculator.calculateCost(cheapest, request) + " KZT"
                + "\nTotal time: " + RouteMetricsCalculator.calculateTime(cheapest) + " min";

        return new RouteDecision(cheapest, explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Cheapest Strategy";
    }
}