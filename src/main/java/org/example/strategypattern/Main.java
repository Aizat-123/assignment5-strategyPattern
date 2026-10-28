package org.example.strategypattern;

public class Main {
    public static void main(String[] args){
        Location warehouse = new Location("Warehouse", 44.85, 65.50);
        Location customer = new Location("Customer", 44.87, 65.53);

        Route route = new Route(
                "R1",
                warehouse,
                customer,
                10.0,  // километры
                25,    // минуты
                1500,  // стоимость
                1.2    // выбросы CO2, кг
        );

        DeliveryRequest request = new DeliveryRequest(
                DeliveryPriority.URGENT,
                3000,
                40,
                false
        );

        System.out.println(route.distanceKm());
        System.out.println(request.maxBudget());
    }
}
