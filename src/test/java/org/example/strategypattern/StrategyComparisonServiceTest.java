package org.example.strategypattern;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StrategyComparisonServiceTest {
    private final StrategyComparisonService service = new StrategyComparisonService();
    private final Location location = new Location("Warehouse", 0, 0);
    private final Route fast = new Route("Fast", location, location, 10, 10, 2000, 3, 0, true);
    private final Route cheap = new Route("Cheap", location, location, 10, 30, 500, 1, 0, true);
    private final List<Route> routes = List.of(fast, cheap);
    private final DeliveryRequest request = new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 60, false);

    @Test
    void comparesDifferentAlgorithmsForSameRequestInGivenOrder() {
        var results = service.compare(List.of(new FastestRouteStrategy(), new CheapestRouteStrategy()), routes, request);
        assertEquals(List.of("Fastest Strategy", "Cheapest Strategy"), new ArrayList<>(results.keySet()));
        assertEquals(fast, results.get("Fastest Strategy").route());
        assertEquals(cheap, results.get("Cheapest Strategy").route());
    }

    @Test
    void includesAllFourStrategiesAndTheirExplanations() {
        List<RouteStrategy> strategies = List.of(new FastestRouteStrategy(), new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(), new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2));
        var results = service.compare(strategies, routes, request);
        assertEquals(4, results.size());
        for (RouteStrategy strategy : strategies) {
            RouteDecision expected = strategy.selectRoute(routes, request);
            assertEquals(expected, results.get(strategy.getStrategyName()));
        }
    }

    @Test
    void propagatesNoSuitableRouteFailure() {
        assertThrows(NoSuitableRouteException.class,
                () -> service.compare(List.of(new FastestRouteStrategy()), List.of(), request));
    }

    @Test
    void rejectsDuplicateNamesInsteadOfOverwritingResult() {
        assertThrows(IllegalArgumentException.class, () -> service.compare(
                List.of(new FastestRouteStrategy(), new FastestRouteStrategy()), routes, request));
    }

    @Test
    void rejectsMissingStrategiesAndRequest() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.compare(List.of(), routes, request)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.compare(null, routes, request)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.compare(List.of(new FastestRouteStrategy()), routes, null))
        );
    }
}
