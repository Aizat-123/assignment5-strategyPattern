package org.example.strategypattern;

import java.util.ArrayList;
import java.util.List;

public class FastestRouteStrategy implements RouteStrategy {
    private final RouteConstraintChecker checker = new RouteConstraintChecker();

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> suitableRoutes = checker.filterRoutes(routes, request);

        Route fastest = suitableRoutes.get(0);
        for (Route route : suitableRoutes) {
            if (RouteMetricsCalculator.calculateTime(route)
                    < RouteMetricsCalculator.calculateTime(fastest)) {
                fastest = route;
            }
        }

        List<String> rejectedRoutes = new ArrayList<>();
        for (Route route : routes) {
            rejectedRoutes.addAll(checker.getRejectionReasons(route, request));
        }

        String explanation = "Selected route: " + fastest.id()
                + "\nReason: minimum total time among suitable routes."
                + "\nEstimated time: " + fastest.estimatedMinutes() + " min"
                + "\nTraffic penalty: +" + fastest.trafficPenaltyMinutes() + " min"
                + "\nTotal time: " + RouteMetricsCalculator.calculateTime(fastest) + " min"
                + "\nTotal cost: " + RouteMetricsCalculator.calculateCost(fastest, request) + " KZT";

        return new RouteDecision(fastest, explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Fastest Strategy";
    }
}
