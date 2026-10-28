package org.example.strategypattern;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Location warehouse = new Location("Warehouse", 44.85, 65.50);
        Location customer = new Location("Customer", 44.87, 65.53);

        Route route = new Route(
                "R1", warehouse, customer,
                10.0, 25, 1500, 1.2,
                5, true
        );
        DeliveryRequest request = new DeliveryRequest(
                DeliveryPriority.URGENT, 3000, 40, false
        );

        System.out.println("Total time: "
                + RouteMetricsCalculator.calculateTime(route) + " min");
        System.out.println("Total cost: "
                + RouteMetricsCalculator.calculateCost(route, request) + " KZT");

        RouteConstraintChecker checker = new RouteConstraintChecker();
        List<Route> suitableRoutes = checker.filterRoutes(List.of(route), request);
        System.out.println("Suitable routes: " + suitableRoutes.size());
    }
}

