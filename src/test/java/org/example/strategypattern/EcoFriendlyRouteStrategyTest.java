package org.example.strategypattern;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EcoFriendlyRouteStrategyTest {
    private final EcoFriendlyRouteStrategy strategy = new EcoFriendlyRouteStrategy();
    private final Location origin = new Location("Warehouse", 44.85, 65.50);
    private final Location destination = new Location("Customer", 44.87, 65.53);
    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.ECO, 5000, 60, true
    );

    private Route route(String id, double km, int minutes, int traffic,
                        double baseCost, double emission, boolean available) {
        return new Route(id, origin, destination, km, minutes, baseCost, emission, traffic, available);
    }

    @Test
    void selectsLowerEmissionForEqualDistance() {
        Route highEmission = route("High", 10, 20, 0, 1000, 3, true);
        Route lowEmission = route("Low", 10, 30, 0, 1000, 1, true);

        assertEquals(lowEmission,
                strategy.selectRoute(List.of(highEmission, lowEmission), request).route());
    }

    @Test
    void distanceWithWeightPointOneCanChangeTheWinner() {
        Route lowEmission = route("LowEmission", 30, 20, 0, 1000, 1, true);
        Route lowerScore = route("LowerScore", 10, 30, 0, 1000, 2, true);

        assertEquals(lowerScore,
                strategy.selectRoute(List.of(lowEmission, lowerScore), request).route());
    }

    @Test
    void rejectsUnavailableRouteEvenWithBestEcoScore() {
        Route unavailable = route("Unavailable", 1, 10, 0, 100, 0, false);
        Route valid = route("Valid", 10, 30, 0, 1000, 1, true);

        RouteDecision decision = strategy.selectRoute(List.of(unavailable, valid), request);

        assertEquals(valid, decision.route());
        assertEquals(List.of("Unavailable: route is unavailable"), decision.rejectedRoutes());
    }

    @Test
    void lowerBudgetCanChangeTheWinner() {
        Route greener = route("Greener", 10, 20, 0, 2000, 0.5, true);
        Route cheaper = route("Cheaper", 10, 30, 0, 1000, 2, true);
        List<Route> routes = List.of(greener, cheaper);
        DeliveryRequest limited = new DeliveryRequest(DeliveryPriority.ECO, 1500, 60, true);

        assertEquals(greener, strategy.selectRoute(routes, request).route());
        RouteDecision decision = strategy.selectRoute(routes, limited);
        assertEquals(cheaper, decision.route());
        assertEquals(List.of("Greener: budget exceeded"), decision.rejectedRoutes());
    }

    @Test
    void rejectsGreenRouteThatMissesDeadlineDueToTraffic() {
        Route late = route("Late", 10, 20, 15, 1000, 0.5, true);
        Route onTime = route("OnTime", 10, 25, 0, 1000, 2, true);
        DeliveryRequest limited = new DeliveryRequest(DeliveryPriority.ECO, 5000, 30, true);

        RouteDecision decision = strategy.selectRoute(List.of(late, onTime), limited);

        assertEquals(onTime, decision.route());
        assertEquals(List.of("Late: delivery deadline exceeded"), decision.rejectedRoutes());
    }

    @Test
    void acceptsExactBudgetAndDeadline() {
        Route exact = route("Exact", 10, 25, 5, 1000, 1, true);
        DeliveryRequest limit = new DeliveryRequest(DeliveryPriority.ECO, 1500, 30, true);

        RouteDecision decision = strategy.selectRoute(List.of(exact), limit);

        assertEquals(exact, decision.route());
        assertTrue(decision.rejectedRoutes().isEmpty());
    }

    @Test
    void keepsFirstRouteWhenScoresAreEqual() {
        Route first = route("First", 10, 30, 0, 1000, 2, true);
        Route second = route("Second", 20, 20, 0, 1000, 1, true);

        assertEquals(first, strategy.selectRoute(List.of(first, second), request).route());
    }

    @Test
    void explainsScoreAndSelectedRoute() {
        Route selected = route("R1", 10, 25, 0, 1000, 2, true);

        RouteDecision decision = strategy.selectRoute(List.of(selected), request);

        assertAll(
                () -> assertTrue(decision.explanation().contains("Selected route: R1")),
                () -> assertTrue(decision.explanation().contains("Carbon emission: 2.0 kg CO2")),
                () -> assertTrue(decision.explanation().contains("Distance: 10.0 km")),
                () -> assertTrue(decision.explanation().contains("distanceKm * 0.1")),
                () -> assertTrue(decision.explanation().contains("Eco score: 3.0"))
        );
    }

    @Test
    void throwsWhenRouteListIsEmpty() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(), request));
    }

    @Test
    void throwsWhenNoRouteMeetsConstraints() {
        Route unavailable = route("Unavailable", 1, 10, 0, 100, 0, false);
        Route expensive = route("Expensive", 10, 20, 0, 6000, 1, true);

        NoSuitableRouteException error = assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(unavailable, expensive), request));

        assertTrue(error.getMessage().contains("Unavailable: route is unavailable"));
        assertTrue(error.getMessage().contains("Expensive: budget exceeded"));
    }
}
