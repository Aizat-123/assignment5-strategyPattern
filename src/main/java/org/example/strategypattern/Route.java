package org.example.strategypattern;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission
) {
    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Route id must not be blank"
            );
        }

        if (origin == null || destination == null) {
            throw new IllegalArgumentException(
                    "Origin and destination are required"
            );
        }

        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException(
                    "Distance must be finite and non-negative"
            );
        }

        if (estimatedMinutes < 0) {
            throw new IllegalArgumentException(
                    "Estimated time must not be negative"
            );
        }

        if (!Double.isFinite(cost) || cost < 0) {
            throw new IllegalArgumentException(
                    "Cost must be finite and non-negative"
            );
        }

        if (!Double.isFinite(carbonEmission) || carbonEmission < 0) {
            throw new IllegalArgumentException(
                    "Carbon emission must be finite and non-negative"
            );
        }
    }
}