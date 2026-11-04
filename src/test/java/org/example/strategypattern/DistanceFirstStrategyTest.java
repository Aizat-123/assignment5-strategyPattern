package org.example.strategypattern;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistanceFirstStrategyTest {
    private final DistanceFirstStrategy strategy =
            new DistanceFirstStrategy();

    private final Location point =
            new Location("Warehouse", 0, 0);

    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 5000, 60, false
    );

    private Route route(
            String id,
            double distance,
            int time,
            double cost,
            boolean available) {

        return new Route(
                id, point, point,
                distance, time, cost, 1, 0, available
        );
    }

    @Test
    void choosesDistanceRatherThanTime() {
        Route shortRoute = route("Short", 5, 40, 1000, true);
        Route fast = route("Fast", 20, 10, 1000, true);

        assertEquals(
                shortRoute,
                strategy.selectRoute(List.of(fast, shortRoute), request)
                        .route()
        );
    }

    @Test
    void rejectsUnavailableShortestRoute() {
        Route unavailable = route("Unavailable", 1, 10, 100, false);
        Route valid = route("Valid", 10, 20, 1000, true);

        RouteDecision decision =
                strategy.selectRoute(List.of(unavailable, valid), request);

        assertEquals(valid, decision.route());
        assertEquals(
                List.of("Unavailable: route is unavailable"),
                decision.rejectedRoutes()
        );
    }

    @Test
    void rejectsShortestRouteOverBudget() {
        Route expensive = route("Expensive", 1, 10, 6000, true);
        Route valid = route("Valid", 10, 20, 1000, true);

        RouteDecision decision =
                strategy.selectRoute(List.of(expensive, valid), request);

        assertEquals(valid, decision.route());
        assertEquals(
                List.of("Expensive: budget exceeded"),
                decision.rejectedRoutes()
        );
    }

    @Test
    void rejectsShortestRouteMissingDeadline() {
        Route late = route("Late", 1, 70, 100, true);
        Route valid = route("Valid", 10, 20, 1000, true);

        RouteDecision decision =
                strategy.selectRoute(List.of(late, valid), request);

        assertEquals(valid, decision.route());
        assertEquals(
                List.of("Late: delivery deadline exceeded"),
                decision.rejectedRoutes()
        );
    }

    @Test
    void keepsFirstOnEqualDistance() {
        Route first = route("First", 10, 30, 1000, true);
        Route second = route("Second", 10, 20, 500, true);

        assertEquals(
                first,
                strategy.selectRoute(List.of(first, second), request)
                        .route()
        );
    }

    @Test
    void acceptsExactLimitsAndExplainsDecision() {
        Route exact = route("Exact", 10, 60, 1000, true);

        DeliveryRequest limit = new DeliveryRequest(
                DeliveryPriority.STANDARD, 1500, 60, false
        );

        RouteDecision decision =
                strategy.selectRoute(List.of(exact), limit);

        assertEquals(exact, decision.route());
        assertTrue(decision.rejectedRoutes().isEmpty());
        assertTrue(
                decision.explanation().contains("Distance: 10.0 km")
        );
    }

    @Test
    void throwsForEmptyOrUnsuitableRoutes() {
        assertThrows(
                NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(), request)
        );

        assertThrows(
                NoSuitableRouteException.class,
                () -> strategy.selectRoute(
                        List.of(route("Closed", 1, 10, 100, false)),
                        request
                )
        );
    }

    @Test
    void worksWithExistingPlannerAndComparisonService() {
        Route shortest = route("Short", 5, 40, 1000, true);
        Route fast = route("Fast", 20, 10, 1000, true);

        List<Route> routes = List.of(fast, shortest);

        RoutePlanner planner =
                new RoutePlanner(new FastestRouteStrategy());

        assertEquals(fast, planner.plan(routes, request).route());

        planner.setStrategy(strategy);

        assertEquals(shortest, planner.plan(routes, request).route());

        var comparison = new StrategyComparisonService().compare(
                List.of(new FastestRouteStrategy(), strategy),
                routes,
                request
        );

        assertEquals(
                shortest,
                comparison.get("Distance First Strategy").route()
        );
        assertEquals(
                fast,
                comparison.get("Fastest Strategy").route()
        );
    }
}