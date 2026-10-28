package org.example.strategypattern;

public record DeliveryRequest(
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        boolean ecoPreferred
) {
    public DeliveryRequest {
        if (priority == null) {
            throw new IllegalArgumentException(
                    "Delivery priority is required"
            );
        }

        if (!Double.isFinite(maxBudget) || maxBudget < 0) {
            throw new IllegalArgumentException(
                    "Budget must be finite and non-negative"
            );
        }

        if (maxDeliveryMinutes < 0) {
            throw new IllegalArgumentException(
                    "Delivery deadline must not be negative"
            );
        }
    }
}