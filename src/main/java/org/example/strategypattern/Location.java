package org.example.strategypattern;

public record Location(
        String name,
        double latitude,
        double longitude
) {
    public Location {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Location name must not be blank"
            );
        }

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }
    }
}
