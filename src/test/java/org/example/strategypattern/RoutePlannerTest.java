package org.example.strategypattern;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutePlannerTest {
    private final Location origin = new Location("Warehouse", 44.85, 65.50);
    private final Location destination = new Location("Customer", 44.87, 65.53);
    private final DeliveryRequest request = new DeliveryRequest(
            DeliveryPriority.STANDARD, 5000, 60, false
    );

    @Test
    void delegatesSameInputsAndReturnsStrategyDecisionUnchanged() {
        Route route = new Route("R1", origin, destination, 10, 25, 1000, 1, 0, true);
        List<Route> routes = List.of(route);
        RouteDecision expected = new RouteDecision(route, "Test decision", List.of());
        RecordingStrategy strategy = new RecordingStrategy(expected);
        RoutePlanner planner = new RoutePlanner(strategy);

        RouteDecision actual = planner.plan(routes, request);

        assertSame(expected, actual);
        assertSame(routes, strategy.receivedRoutes);
        assertSame(request, strategy.receivedRequest);
    }

    @Test
    void samePlannerSupportsThreeRuntimeSwitches() {
        Route fast = new Route("Fast", origin, destination, 30, 10, 1500, 3, 0, true);
        Route cheap = new Route("Cheap", origin, destination, 20, 40, 0, 4, 0, true);
        Route eco = new Route("Eco", origin, destination, 10, 35, 1500, 0.2, 0, true);
        Route balanced = new Route("Balanced", origin, destination, 15, 20, 750, 1, 0, true);
        List<Route> routes = List.of(fast, cheap, eco, balanced);
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        assertEquals(fast, planner.plan(routes, request).route());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals(cheap, planner.plan(routes, request).route());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals(eco, planner.plan(routes, request).route());

        planner.setStrategy(new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2));
        assertEquals(balanced, planner.plan(routes, request).route());
    }

    @Test
    void rejectsMissingInitialStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new RoutePlanner(null));
    }

    @Test
    void rejectedNullSwitchPreservesPreviousStrategy() {
        Route route = new Route("R1", origin, destination, 10, 25, 1000, 1, 0, true);
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        assertThrows(IllegalArgumentException.class, () -> planner.setStrategy(null));
        assertEquals(route, planner.plan(List.of(route), request).route());
    }

    @Test
    void reportsNameOfCurrentStrategy() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        assertEquals("Fastest Strategy", planner.getStrategyName());
        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("Cheapest Strategy", planner.getStrategyName());
    }

    @Test
    void propagatesNoSuitableRouteFailure() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        assertThrows(NoSuitableRouteException.class, () -> planner.plan(List.of(), request));
    }

    private static class RecordingStrategy implements RouteStrategy {
        private final RouteDecision decision;
        private List<Route> receivedRoutes;
        private DeliveryRequest receivedRequest;

        private RecordingStrategy(RouteDecision decision) {
            this.decision = decision;
        }

        @Override
        public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
            receivedRoutes = routes;
            receivedRequest = request;
            return decision;
        }

        @Override
        public String getStrategyName() {
            return "Recording Strategy";
        }
    }
}
