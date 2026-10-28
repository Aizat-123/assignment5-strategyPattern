package org.example.strategypattern;

public final class RouteMetricsCalculator {
    private static final double PRICE_PER_KM = 50;
    private static final double URGENT_SURCHARGE = 500;

    private RouteMetricsCalculator() {
    }

    public static long calculateTime(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Route is required");
        }
        return (long) route.estimatedMinutes() + route.trafficPenaltyMinutes();
    }

    public static double calculateCost(Route route, DeliveryRequest request) {
        if (route == null || request == null) {
            throw new IllegalArgumentException("Route and request are required");
        }
        double surcharge = request.priority() == DeliveryPriority.URGENT
                ? URGENT_SURCHARGE : 0;
        double total = route.cost() + route.distanceKm() * PRICE_PER_KM + surcharge;
        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("Calculated cost must be finite");
        }
        return total;
    }
}