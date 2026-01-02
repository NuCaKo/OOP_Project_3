package com.group27.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.group27.core.DatabaseAdapter;
import com.group27.controller.CartController.CartItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Utility class for generating PDF invoices and saving them to the database.
 * Uses iText library to create PDF documents containing order details.
 * 
 * @author Group27
 * @version 1.0
 */
public class InvoiceGenerator {

    /**
     * Generates a PDF invoice for an order and saves it to the database.
     * The invoice includes order ID, list of items with quantities and prices,
     * and the total cost. The PDF is stored as a Base64-encoded string in the database.
     * 
     * @param orderId The ID of the order for which to generate the invoice
     * @param items List of CartItem objects representing the products in the order
     * @param totalCost The total cost of the order
     */
    public static void generateAndSaveInvoice(int orderId, List<CartItem> items, double totalCost) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("INVOICE"));
            document.add(new Paragraph("Order ID: " + orderId));
            document.add(new Paragraph("------------------------------------------------"));
            
            for (CartItem item : items) {
                document.add(new Paragraph(
                    item.getProduct().getName() + " - " + 
                    item.getAmount() + "kg x $" + item.getPriceAtMoment() + 
                    " = $" + String.format("%.2f", item.getTotal())
                ));
            }
            
            document.add(new Paragraph("------------------------------------------------"));
            document.add(new Paragraph("Total Cost: $" + String.format("%.2f", totalCost)));
            document.add(new Paragraph("Thank you for your business!"));

            document.close();
            
            // Save to DB
            saveInvoiceToDB(orderId, new ByteArrayInputStream(out.toByteArray()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the generated PDF invoice to the database.
     * Converts the PDF to Base64 encoding and stores it in the OrderInfo table.
     * 
     * @param orderId The ID of the order to update with the invoice
     * @param pdfStream InputStream containing the PDF data
     */
    private static void saveInvoiceToDB(int orderId, ByteArrayInputStream pdfStream) {
        String query = "UPDATE OrderInfo SET invoice = ? WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
             
            // Using setBlob or setBinaryStream for MEDIUMTEXT/BLOB column
            // The schema said MEDIUMTEXT (CLOB), but requirements said CLOB. 
            // MySQL TEXT/CLOB usually takes String. If it's binary PDF, it should be BLOB.
            // The prompt said "invoices should be stored as CLOBs". Storing binary PDF in CLOB is tricky (Base64).
            // I will store it as Base64 string in the CLOB column to respect the CLOB requirement while keeping binary data.
            
            byte[] pdfBytes = new byte[pdfStream.available()];
            pdfStream.read(pdfBytes);
            String base64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            
            stmt.setString(1, base64);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
