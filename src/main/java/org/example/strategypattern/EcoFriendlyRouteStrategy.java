package org.example.strategypattern;

import java.util.ArrayList;
import java.util.List;

public class EcoFriendlyRouteStrategy implements RouteStrategy {
    private static final double DISTANCE_WEIGHT = 0.1;
    private final RouteConstraintChecker checker = new RouteConstraintChecker();

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> suitableRoutes = checker.filterRoutes(routes, request);

        Route ecoRoute = suitableRoutes.get(0);
        for (Route route : suitableRoutes) {
            if (calculateEcoScore(route) < calculateEcoScore(ecoRoute)) {
                ecoRoute = route;
            }
        }

        List<String> rejectedRoutes = new ArrayList<>();
        for (Route route : routes) {
            rejectedRoutes.addAll(checker.getRejectionReasons(route, request));
        }

        String explanation = "Selected route: " + ecoRoute.id()
                + "\nReason: minimum eco score among suitable routes."
                + "\nCarbon emission: " + ecoRoute.carbonEmission() + " kg CO2"
                + "\nDistance: " + ecoRoute.distanceKm() + " km"
                + "\nEco score = carbonEmission + distanceKm * " + DISTANCE_WEIGHT
                + "\nEco score: " + calculateEcoScore(ecoRoute)
                + "\nTotal cost: " + RouteMetricsCalculator.calculateCost(ecoRoute, request) + " KZT"
                + "\nTotal time: " + RouteMetricsCalculator.calculateTime(ecoRoute) + " min";

        return new RouteDecision(ecoRoute, explanation, rejectedRoutes);
    }

    private double calculateEcoScore(Route route) {
        double score = route.carbonEmission() + route.distanceKm() * DISTANCE_WEIGHT;
        if (!Double.isFinite(score)) {
            throw new IllegalArgumentException("Eco score must be finite");
        }
        return score;
    }

    @Override
    public String getStrategyName() {
        return "Eco Friendly Strategy";
    }
}
