package com.example.suppliersla.demo;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DemoDataService {
    private final List<Map<String, Object>> orders = new ArrayList<>();

    public DemoDataService() {
        add("A-01","Supplier A","2026-10-01","2026-10-01",100000,1000,1000,"COMPLIANT",0,"100% accepted on Oct 1.");
        add("A-02","Supplier A","2026-10-07","2026-10-09",100000,1000,1000,"COMPLIANT",0,"Completed exactly on the last grace day, Oct 9.");
        add("A-03","Supplier A","2026-10-09","2026-10-10",100000,1000,1000,"COMPLIANT",0,"Completed before Oct 11 grace deadline.");
        add("A-04","Supplier A","2026-10-08","2026-10-13",100000,1000,1000,"LATE",1500,"600 units accepted Oct 10; remaining 400 accepted Oct 13. Completion was 3 days after Oct 10 deadline.");
        add("A-05","Supplier A","2026-10-15","2026-10-15",100000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("A-06","Supplier A","2026-10-22","2026-10-22",100000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("A-07","Supplier A","2026-10-27","2026-10-28",100000,1000,1000,"COMPLIANT",0,"Completed within grace period.");

        add("B-01","Supplier B","2026-10-01","2026-10-01",150000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("B-02","Supplier B","2026-10-06","2026-10-07",150000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("B-03","Supplier B","2026-10-13","2026-10-18",150000,1000,1000,"COMPLIANT",0,"Approved port-strike extension of 3 days; adjusted deadline Oct 18.");
        add("B-04","Supplier B","2026-10-16","2026-10-17",150000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("B-05","Supplier B","2026-10-21","2026-10-26",150000,1000,1000,"LATE",4500,"70% accepted Oct 22; remaining 30% accepted Oct 26. Completion was 3 days after Oct 23 deadline.");
        add("B-06","Supplier B","2026-10-24","2026-10-25",150000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("B-07","Supplier B","2026-10-29","2026-10-29",150000,1000,1000,"COMPLIANT",0,"Completed within grace period.");

        add("C-01","Supplier C","2026-10-01","2026-10-01",200000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("C-02","Supplier C","2026-10-05","2026-10-05",200000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("C-03","Supplier C","2026-10-08","2026-10-09",200000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
        add("C-04","Supplier C","2026-10-09","2026-10-14",200000,1000,1000,"LATE",4500,"500 units accepted Oct 11; remaining quantity accepted Oct 14 after receipt verification. Three days late.");
        add("C-05","Supplier C","2026-10-19","2026-10-21",200000,1000,1000,"COMPLIANT",0,"Completed on the last grace day.");
        add("C-06","Supplier C","2026-10-27","2026-10-28",200000,1000,1000,"COMPLIANT",0,"Completed within grace period.");
    }

    private void add(String id, String supplier, String promised, String completed,
                     int value, int ordered, int accepted, String status, int penalty, String reason) {
        orders.add(Map.of(
            "orderNumber", id, "supplierName", supplier, "promisedDate", promised,
            "completionDate", completed, "orderValue", value, "orderedQuantity", ordered,
            "acceptedQuantity", accepted, "status", status, "penalty", penalty, "explanation", reason
        ));
    }

    public List<Map<String, Object>> orders() {
        return List.copyOf(orders);
    }

    public Map<String, Object> explanation(String orderNumber) {
        return orders.stream().filter(o -> orderNumber.equals(o.get("orderNumber")))
            .findFirst().orElseThrow(() -> new NoSuchElementException("Order not found: " + orderNumber));
    }

    public Map<String, Object> monthlyReport(String month) {
        List<Map<String, Object>> suppliers = List.of(
            supplier("Supplier A", 7, 6, 1, 85.7143, 700000, 1500),
            supplier("Supplier B", 7, 6, 1, 85.7143, 1050000, 4500),
            supplier("Supplier C", 6, 5, 1, 83.3333, 1200000, 4500)
        );
        return Map.of(
            "month", month,
            "totalOrders", 20,
            "compliantOrders", 17,
            "failedOrders", 3,
            "forceMajeureApprovedOrders", 1,
            "recommendedPenalties", 10500,
            "suppliers", suppliers,
            "orders", orders
        );
    }

    private Map<String, Object> supplier(String name, int total, int compliant, int failed,
                                         double otif, int monthlyValue, int penalty) {
        return Map.of("supplierName", name, "totalOrders", total, "compliantOrders", compliant,
            "failedOrders", failed, "otifPercentage", otif, "monthlyOrderValue", monthlyValue,
            "recommendedPenalty", penalty, "thresholdBreached", true);
    }
}
