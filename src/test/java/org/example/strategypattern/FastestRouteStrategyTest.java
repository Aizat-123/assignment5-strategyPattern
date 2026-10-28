package org.example.strategypattern;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FastestRouteStrategyTest {
    private final FastestRouteStrategy strategy = new FastestRouteStrategy();
    private final Location origin = new Location("Warehouse", 44.85, 65.50);
    private final Location destination = new Location("Customer", 44.87, 65.53);
    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 5000, 60, false
    );

    private Route route(String id, int minutes, int traffic, double baseCost, boolean available) {
        return new Route(id, origin, destination, 10, minutes, baseCost, 1.2, traffic, available);
    }

    @Test
    void selectsRouteWithMinimumTime() {
        Route slow = route("Slow", 40, 0, 1000, true);
        Route fast = route("Fast", 20, 0, 1000, true);

        assertEquals(fast, strategy.selectRoute(List.of(slow, fast), request).route());
    }

    @Test
    void trafficCanChangeTheWinner() {
        Route congested = route("Congested", 15, 20, 1000, true);
        Route clear = route("Clear", 25, 0, 1000, true);

        assertEquals(clear, strategy.selectRoute(List.of(congested, clear), request).route());
    }

    @Test
    void rejectsUnavailableRouteEvenIfItIsFaster() {
        Route unavailable = route("Unavailable", 5, 0, 1000, false);
        Route available = route("Available", 30, 0, 1000, true);

        RouteDecision decision = strategy.selectRoute(List.of(unavailable, available), request);

        assertEquals(available, decision.route());
        assertEquals(List.of("Unavailable: route is unavailable"), decision.rejectedRoutes());
    }

    @Test
    void budgetChangeCanChangeTheWinner() {
        Route expensive = route("Expensive", 10, 0, 2000, true);
        Route cheap = route("Cheap", 30, 0, 1000, true);
        List<Route> routes = List.of(expensive, cheap);
        DeliveryRequest limited = new DeliveryRequest(DeliveryPriority.STANDARD, 1500, 60, false);

        assertEquals(expensive, strategy.selectRoute(routes, request).route());
        RouteDecision decision = strategy.selectRoute(routes, limited);
        assertEquals(cheap, decision.route());
        assertEquals(List.of("Expensive: budget exceeded"), decision.rejectedRoutes());
    }

    @Test
    void deadlineIncludesTrafficPenalty() {
        Route late = route("Late", 10, 25, 1000, true);
        Route onTime = route("OnTime", 25, 0, 1000, true);
        DeliveryRequest limited = new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 30, false);

        RouteDecision decision = strategy.selectRoute(List.of(late, onTime), limited);

        assertEquals(onTime, decision.route());
        assertEquals(List.of("Late: delivery deadline exceeded"), decision.rejectedRoutes());
    }

    @Test
    void acceptsExactBudgetAndDeadline() {
        Route exact = route("Exact", 25, 5, 1000, true);
        DeliveryRequest limit = new DeliveryRequest(DeliveryPriority.URGENT, 2000, 30, false);

        RouteDecision decision = strategy.selectRoute(List.of(exact), limit);

        assertEquals(exact, decision.route());
        assertTrue(decision.rejectedRoutes().isEmpty());
    }

    @Test
    void keepsFirstRouteWhenTotalTimesAreEqual() {
        Route first = route("First", 20, 10, 1000, true);
        Route second = route("Second", 30, 0, 1000, true);

        assertEquals(first, strategy.selectRoute(List.of(first, second), request).route());
    }

    @Test
    void explainsSelectedRouteAndTrafficPenalty() {
        Route selected = route("R1", 25, 5, 1000, true);

        RouteDecision decision = strategy.selectRoute(List.of(selected), request);

        assertAll(
                () -> assertTrue(decision.explanation().contains("Selected route: R1")),
                () -> assertTrue(decision.explanation().contains("Estimated time: 25 min")),
                () -> assertTrue(decision.explanation().contains("Traffic penalty: +5 min")),
                () -> assertTrue(decision.explanation().contains("Total time: 30 min"))
        );
    }

    @Test
    void throwsWhenRouteListIsEmpty() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(), request));
    }

    @Test
    void throwsWhenNoRouteMeetsConstraints() {
        Route unavailable = route("Unavailable", 10, 0, 1000, false);
        Route overBudget = route("Expensive", 20, 0, 6000, true);

        NoSuitableRouteException error = assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(unavailable, overBudget), request));

        assertTrue(error.getMessage().contains("Unavailable: route is unavailable"));
        assertTrue(error.getMessage().contains("Expensive: budget exceeded"));
    }
}
