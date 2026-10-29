package org.example.strategypattern;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CheapestRouteStrategyTest {
    private final CheapestRouteStrategy strategy = new CheapestRouteStrategy();
    private final Location origin = new Location("Warehouse", 44.85, 65.50);
    private final Location destination = new Location("Customer", 44.87, 65.53);
    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.ECONOMY, 5000, 60, false
    );

    private Route route(String id, double km, int minutes, int traffic,
                        double baseCost, boolean available) {
        return new Route(id, origin, destination, km, minutes, baseCost, 1.2, traffic, available);
    }

    @Test
    void selectsMinimumTotalCost() {
        Route expensive = route("Expensive", 10, 20, 0, 2000, true);
        Route cheap = route("Cheap", 10, 30, 0, 1000, true);

        assertEquals(cheap, strategy.selectRoute(List.of(expensive, cheap), request).route());
    }

    @Test
    void distanceCostCanChangeTheWinner() {
        Route lowBase = route("LowBase", 30, 20, 0, 500, true);
        Route lowTotal = route("LowTotal", 10, 30, 0, 1000, true);

        assertEquals(lowTotal, strategy.selectRoute(List.of(lowBase, lowTotal), request).route());
    }

    @Test
    void rejectsUnavailableCheapestRoute() {
        Route unavailable = route("Unavailable", 1, 10, 0, 100, false);
        Route valid = route("Valid", 10, 30, 0, 1000, true);

        RouteDecision decision = strategy.selectRoute(List.of(unavailable, valid), request);

        assertEquals(valid, decision.route());
        assertEquals(List.of("Unavailable: route is unavailable"), decision.rejectedRoutes());
    }

    @Test
    void rejectsCheaperRouteThatMissesDeadlineDueToTraffic() {
        Route late = route("Late", 1, 20, 15, 100, true);
        Route onTime = route("OnTime", 10, 25, 0, 1000, true);
        DeliveryRequest limited = new DeliveryRequest(DeliveryPriority.ECONOMY, 5000, 30, false);

        RouteDecision decision = strategy.selectRoute(List.of(late, onTime), limited);

        assertEquals(onTime, decision.route());
        assertEquals(List.of("Late: delivery deadline exceeded"), decision.rejectedRoutes());
    }

    @Test
    void urgentSurchargeIsIncludedInBudgetRestriction() {
        Route only = route("R1", 10, 25, 0, 1000, true);
        DeliveryRequest economy = new DeliveryRequest(DeliveryPriority.ECONOMY, 1500, 60, false);
        DeliveryRequest urgent = new DeliveryRequest(DeliveryPriority.URGENT, 1500, 60, false);

        assertEquals(only, strategy.selectRoute(List.of(only), economy).route());
        NoSuitableRouteException error = assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(only), urgent));
        assertTrue(error.getMessage().contains("R1: budget exceeded"));
    }

    @Test
    void acceptsExactBudgetAndDeadline() {
        Route exact = route("Exact", 10, 25, 5, 1000, true);
        DeliveryRequest limit = new DeliveryRequest(DeliveryPriority.URGENT, 2000, 30, false);

        RouteDecision decision = strategy.selectRoute(List.of(exact), limit);

        assertEquals(exact, decision.route());
        assertTrue(decision.rejectedRoutes().isEmpty());
    }

    @Test
    void keepsFirstRouteWhenTotalCostsAreEqual() {
        Route first = route("First", 10, 30, 0, 1000, true);
        Route second = route("Second", 20, 20, 0, 500, true);

        assertEquals(first, strategy.selectRoute(List.of(first, second), request).route());
    }

    @Test
    void explainsSelectedRouteAndTotalCost() {
        Route selected = route("R1", 10, 25, 0, 1000, true);

        RouteDecision decision = strategy.selectRoute(List.of(selected), request);

        assertAll(
                () -> assertTrue(decision.explanation().contains("Selected route: R1")),
                () -> assertTrue(decision.explanation().contains("Base cost: 1000.0 KZT")),
                () -> assertTrue(decision.explanation().contains("Distance: 10.0 km")),
                () -> assertTrue(decision.explanation().contains("Total cost: 1500.0 KZT"))
        );
    }

    @Test
    void throwsWhenRouteListIsEmpty() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(), request));
    }

    @Test
    void fastestAndCheapestCanChooseDifferentRoutesForSameRequest() {
        Route fast = route("Fast", 10, 15, 0, 2000, true);
        Route cheap = route("Cheap", 10, 40, 0, 1000, true);
        List<Route> routes = List.of(fast, cheap);
        RouteStrategy fastestStrategy = new FastestRouteStrategy();
        RouteStrategy cheapestStrategy = strategy;

        assertEquals(fast, fastestStrategy.selectRoute(routes, request).route());
        assertEquals(cheap, cheapestStrategy.selectRoute(routes, request).route());
    }
}
