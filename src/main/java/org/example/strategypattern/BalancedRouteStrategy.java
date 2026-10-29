package org.example.strategypattern;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class BalancedRouteStrategy implements RouteStrategy {
    private static final double EPSILON = 1e-9;

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;
    private final RouteConstraintChecker checker = new RouteConstraintChecker();

    public BalancedRouteStrategy(double timeWeight, double costWeight,
                                 double distanceWeight, double emissionWeight) {
        double[] weights = {timeWeight, costWeight, distanceWeight, emissionWeight};
        for (double weight : weights) {
            if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
                throw new IllegalArgumentException("Each weight must be finite and between 0 and 1");
            }
        }
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > EPSILON) {
            throw new IllegalArgumentException("Weights must sum to 1.0");
        }

        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> suitableRoutes = checker.filterRoutes(routes, request);

        DoubleSummaryStatistics times = new DoubleSummaryStatistics();
        DoubleSummaryStatistics costs = new DoubleSummaryStatistics();
        DoubleSummaryStatistics distances = new DoubleSummaryStatistics();
        DoubleSummaryStatistics emissions = new DoubleSummaryStatistics();

        for (Route route : suitableRoutes) {
            times.accept(RouteMetricsCalculator.calculateTime(route));
            costs.accept(RouteMetricsCalculator.calculateCost(route, request));
            distances.accept(route.distanceKm());
            emissions.accept(route.carbonEmission());
        }

        Route balanced = suitableRoutes.get(0);
        double bestScore = Double.POSITIVE_INFINITY;

        for (Route route : suitableRoutes) {
            double normalizedTime = normalize(RouteMetricsCalculator.calculateTime(route), times);
            double normalizedCost = normalize(RouteMetricsCalculator.calculateCost(route, request), costs);
            double normalizedDistance = normalize(route.distanceKm(), distances);
            double normalizedEmission = normalize(route.carbonEmission(), emissions);

            double score = normalizedTime * timeWeight
                    + normalizedCost * costWeight
                    + normalizedDistance * distanceWeight
                    + normalizedEmission * emissionWeight;

            if (score < bestScore) {
                balanced = route;
                bestScore = score;
            }
        }

        List<String> rejectedRoutes = new ArrayList<>();
        for (Route route : routes) {
            rejectedRoutes.addAll(checker.getRejectionReasons(route, request));
        }

        String explanation = "Selected route: " + balanced.id()
                + "\nReason: minimum weighted score among suitable routes."
                + "\nNormalization: (value - min) / (max - min); equal values contribute 0."
                + "\nNormalized time: " + normalize(RouteMetricsCalculator.calculateTime(balanced), times)
                + "; weight: " + timeWeight
                + "\nNormalized cost: " + normalize(RouteMetricsCalculator.calculateCost(balanced, request), costs)
                + "; weight: " + costWeight
                + "\nNormalized distance: " + normalize(balanced.distanceKm(), distances)
                + "; weight: " + distanceWeight
                + "\nNormalized emission: " + normalize(balanced.carbonEmission(), emissions)
                + "; weight: " + emissionWeight
                + "\nBalanced score: " + bestScore
                + "\nTotal time: " + RouteMetricsCalculator.calculateTime(balanced) + " min"
                + "\nTotal cost: " + RouteMetricsCalculator.calculateCost(balanced, request) + " KZT";

        return new RouteDecision(balanced, explanation, rejectedRoutes);
    }

    private double normalize(double value, DoubleSummaryStatistics statistics) {
        double min = statistics.getMin();
        double max = statistics.getMax();
        if (min == max) {
            return 0;
        }
        return (value - min) / (max - min);
    }

    @Override
    public String getStrategyName() {
        return "Balanced Strategy";
    }
}
