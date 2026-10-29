package org.example.strategypattern;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BalancedRouteStrategyTest {
    private final Location origin = new Location("Warehouse", 44.85, 65.50);
    private final Location destination = new Location("Customer", 44.87, 65.53);
    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 5000, 60, false
    );
    private final BalancedRouteStrategy strategy = new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
    private final Route fast = route("Fast", 30, 10, 0, 1500, 3, true);
    private final Route cheap = route("Cheap", 10, 30, 0, 500, 1, true);
    private final Route middle = route("Middle", 20, 20, 0, 500, 1.5, true);

    private Route route(String id, double km, int minutes, int traffic,
                        double baseCost, double emission, boolean available) {
        return new Route(id, origin, destination, km, minutes, baseCost, emission, traffic, available);
    }

    @Test
    void selectsCompromiseUsingAllFourCriteria() {
        RouteDecision decision = strategy.selectRoute(List.of(fast, cheap, middle), request);

        assertEquals(middle, decision.route());
        assertTrue(decision.explanation().contains("Balanced score: 0.375"));
    }

    @Test
    void changingWeightsCanChangeTheWinner() {
        List<Route> routes = List.of(fast, cheap, middle);

        assertEquals(fast, new BalancedRouteStrategy(1, 0, 0, 0).selectRoute(routes, request).route());
        assertEquals(cheap, new BalancedRouteStrategy(0, 1, 0, 0).selectRoute(routes, request).route());
    }

    @Test
    void distanceAndEmissionWeightsCanEachDetermineTheWinner() {
        Route shorter = route("Shorter", 5, 20, 0, 1000, 3, true);
        Route greener = route("Greener", 15, 20, 0, 500, 1, true);
        List<Route> routes = List.of(shorter, greener);

        assertEquals(shorter, new BalancedRouteStrategy(0, 0, 1, 0).selectRoute(routes, request).route());
        assertEquals(greener, new BalancedRouteStrategy(0, 0, 0, 1).selectRoute(routes, request).route());
    }

    @Test
    void timeCriterionIncludesTrafficPenalty() {
        Route congested = route("Congested", 10, 10, 30, 1000, 1, true);
        Route clear = route("Clear", 10, 25, 0, 1000, 1, true);

        assertEquals(clear, new BalancedRouteStrategy(1, 0, 0, 0)
                .selectRoute(List.of(congested, clear), request).route());
    }

    @Test
    void excludedRouteDoesNotAffectNormalization() {
        Route unavailable = route("Unavailable", 10, 1000, 0, 500, 1, false);
        BalancedRouteStrategy timeAndCost = new BalancedRouteStrategy(0.6, 0.4, 0, 0);

        RouteDecision without = timeAndCost.selectRoute(List.of(fast, cheap), request);
        RouteDecision with = timeAndCost.selectRoute(List.of(fast, cheap, unavailable), request);

        assertEquals(fast, without.route());
        assertEquals(without.route(), with.route());
        assertEquals(List.of("Unavailable: route is unavailable", "Unavailable: delivery deadline exceeded"),
                with.rejectedRoutes());
    }

    @Test
    void budgetConstraintOverridesPreferredScore() {
        DeliveryRequest limited = new DeliveryRequest(DeliveryPriority.STANDARD, 2000, 60, false);
        RouteDecision decision = new BalancedRouteStrategy(1, 0, 0, 0)
                .selectRoute(List.of(fast, middle), limited);

        assertEquals(middle, decision.route());
        assertEquals(List.of("Fast: budget exceeded"), decision.rejectedRoutes());
    }

    @Test
    void acceptsExactBudgetAndDeadlineWithSingleCandidate() {
        Route exact = route("Exact", 10, 25, 5, 1000, 1, true);
        DeliveryRequest limit = new DeliveryRequest(DeliveryPriority.URGENT, 2000, 30, false);

        RouteDecision decision = strategy.selectRoute(List.of(exact), limit);

        assertEquals(exact, decision.route());
        assertTrue(decision.rejectedRoutes().isEmpty());
        assertTrue(decision.explanation().contains("Balanced score: 0.0"));
    }

    @Test
    void equalValuesDoNotCauseDivisionByZeroAndKeepFirstRoute() {
        Route first = route("First", 10, 25, 5, 1000, 1, true);
        Route second = route("Second", 10, 25, 5, 1000, 1, true);

        RouteDecision decision = strategy.selectRoute(List.of(first, second), request);

        assertEquals(first, decision.route());
        assertTrue(decision.explanation().contains("Balanced score: 0.0"));
        assertFalse(decision.explanation().contains("NaN"));
    }

    @Test
    void rejectsNegativeWeightEvenIfSumIsOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3));
    }

    @Test
    void rejectsWeightsWithWrongSum() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(0.4, 0.4, 0.1, 0.2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(0, 0, 0, 0))
        );
    }

    @Test
    void rejectsNonFiniteWeights() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(Double.NaN, 0.3, 0.1, 0.2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(0.4, Double.POSITIVE_INFINITY, 0.1, 0.2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(0.4, 0.3, Double.NaN, 0.2)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BalancedRouteStrategy(0.4, 0.3, 0.1, Double.NEGATIVE_INFINITY))
        );
    }

    @Test
    void acceptsValidDecimalWeightsDespiteFloatingPointRounding() {
        BalancedRouteStrategy decimalWeights = new BalancedRouteStrategy(0.1, 0.2, 0.3, 0.4);

        assertEquals(middle, decimalWeights.selectRoute(List.of(middle), request).route());
    }

    @Test
    void throwsWhenRouteListIsEmpty() {
        assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(), request));
    }

    @Test
    void throwsWhenAllRoutesMissDeadline() {
        DeliveryRequest impossible = new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 5, false);

        NoSuitableRouteException error = assertThrows(NoSuitableRouteException.class,
                () -> strategy.selectRoute(List.of(fast, cheap, middle), impossible));

        assertTrue(error.getMessage().contains("delivery deadline exceeded"));
    }
}
