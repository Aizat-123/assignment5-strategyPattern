package org.example.strategypattern;

import java.util.ArrayList;
import java.util.List;

public class RouteConstraintChecker {
    public List<String> getRejectionReasons(Route route, DeliveryRequest request) {
        if (route == null || request == null) {
            throw new IllegalArgumentException("Route and request are required");
        }

        List<String> reasons = new ArrayList<>();
        if (!route.available()) {
            reasons.add(route.id() + ": route is unavailable");
        }
        if (RouteMetricsCalculator.calculateCost(route, request) > request.maxBudget()) {
            reasons.add(route.id() + ": budget exceeded");
        }
        if (RouteMetricsCalculator.calculateTime(route) > request.maxDeliveryMinutes()) {
            reasons.add(route.id() + ": delivery deadline exceeded");
        }
        return List.copyOf(reasons);
    }

    public List<Route> filterRoutes(List<Route> routes, DeliveryRequest request) {
        if (routes == null || request == null) {
            throw new IllegalArgumentException("Routes and request are required");
        }

        List<Route> suitableRoutes = new ArrayList<>();
        List<String> rejectionReasons = new ArrayList<>();
        for (Route route : routes) {
            List<String> reasons = getRejectionReasons(route, request);
            if (reasons.isEmpty()) {
                suitableRoutes.add(route);
            } else {
                rejectionReasons.addAll(reasons);
            }
        }

        if (suitableRoutes.isEmpty()) {
            String details = routes.isEmpty()
                    ? "Route list is empty"
                    : String.join("; ", rejectionReasons);
            throw new NoSuitableRouteException("No suitable route. " + details);
        }
        return List.copyOf(suitableRoutes);
    }
}

