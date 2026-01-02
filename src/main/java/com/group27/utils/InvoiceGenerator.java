package com.group27.utils;

/**
 * Utility class for generating invoices.
 */
public class InvoiceGenerator {

    /**
     * Generates a simple text invoice.
     *
     * @param orderId   The order ID.
     * @param customer  The customer's name.
     * @param products  The list of products and quantities.
     * @param total     The total cost.
     * @return A formatted invoice string.
     */
    public static String generateInvoice(int orderId, String customer, String products, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("----------- INVOICE -----------\n");
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Customer: ").append(customer).append("\n");
        sb.append("Date: ").append(new java.util.Date()).append("\n");
        sb.append("-------------------------------\n");
        sb.append("Items:\n").append(products.replace(";", "\n")).append("\n");
        sb.append("-------------------------------\n");
        sb.append("TOTAL: $").append(String.format("%.2f", total)).append("\n");
        sb.append("-------------------------------\n");
        sb.append("Thank you for shopping with us!");
        return sb.toString();
    }
}
