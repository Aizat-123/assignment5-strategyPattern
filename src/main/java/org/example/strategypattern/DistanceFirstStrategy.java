package org.example.strategypattern;

import java.util.ArrayList;
import java.util.List;

public class DistanceFirstStrategy implements RouteStrategy {
    private final RouteConstraintChecker checker =
            new RouteConstraintChecker();

    @Override
    public RouteDecision selectRoute(
            List<Route> routes,
            DeliveryRequest request) {

        List<Route> suitableRoutes =
                checker.filterRoutes(routes, request);

        Route shortest = suitableRoutes.get(0);

        for (Route route : suitableRoutes) {
            if (route.distanceKm() < shortest.distanceKm()) {
                shortest = route;
            }
        }

        List<String> rejectedRoutes = new ArrayList<>();

        for (Route route : routes) {
            rejectedRoutes.addAll(
                    checker.getRejectionReasons(route, request)
            );
        }

        String explanation = "Selected route: " + shortest.id()
                + "\nReason: minimum distance among suitable routes."
                + "\nDistance: " + shortest.distanceKm() + " km"
                + "\nTotal time: "
                + RouteMetricsCalculator.calculateTime(shortest) + " min"
                + "\nTotal cost: "
                + RouteMetricsCalculator.calculateCost(shortest, request)
                + " KZT";

        return new RouteDecision(
                shortest,
                explanation,
                rejectedRoutes
        );
    }

    @Override
    public String getStrategyName() {
        return "Distance First Strategy";
    }
}