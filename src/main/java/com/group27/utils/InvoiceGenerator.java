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

public class InvoiceGenerator {

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
