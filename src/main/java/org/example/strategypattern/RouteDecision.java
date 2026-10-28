package org.example.strategypattern;

import java.util.List;

public record RouteDecision(
        Route route,
        String explanation,
        List<String> rejectedRoutes
) {
    public RouteDecision {
        if (route == null) {
            throw new IllegalArgumentException("Selected route is required");
        }

        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException(
                    "Explanation must not be blank"
            );
        }

        if (rejectedRoutes == null) {
            throw new IllegalArgumentException(
                    "Rejected routes list is required"
            );
        }

        for (String reason : rejectedRoutes) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(
                        "Rejection reason must not be blank"
                );
            }
        }

        rejectedRoutes = List.copyOf(rejectedRoutes);
    }
}