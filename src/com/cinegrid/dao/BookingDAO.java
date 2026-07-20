package com.cinegrid.dao;

import com.cinegrid.config.DBConfig;
import com.cinegrid.model.Seat;
import java.sql.*;
import java.util.*;

public class BookingDAO {
    public boolean bookSelectedSeats(Set<Integer> seatIds, int userId, int showId, double totalAmount, String seatLabels) throws SQLException {
        Connection conn = null; PreparedStatement bkStmt = null; PreparedStatement updStmt = null;
        try {
            conn = DBConfig.getConnection(); conn.setAutoCommit(false);
            
            // 💡 FIX: Yahan explicit CONVERT_TZ ya NOW() ke sath current Indian time / local time insert karwa rahe hain
            String bkSql = "INSERT INTO bookings (user_id, show_id, seats_booked, total_amount, status, booking_time) VALUES (?, ?, ?, ?, 'CONFIRMED', CONVERT_TZ(NOW(), '+00:00', '+05:30'))";
            
            bkStmt = conn.prepareStatement(bkSql, Statement.RETURN_GENERATED_KEYS);
            bkStmt.setInt(1, userId); 
            bkStmt.setInt(2, showId); 
            bkStmt.setString(3, seatLabels); 
            bkStmt.setDouble(4, totalAmount);
            bkStmt.executeUpdate();
            
            ResultSet rs = bkStmt.getGeneratedKeys(); int bookingId = 0; if(rs.next()) bookingId = rs.getInt(1);
            
            String updSql = "UPDATE seats SET is_booked = true, booked_by_user_id = ?, booking_id = ? WHERE id = ?";
            updStmt = conn.prepareStatement(updSql);
            for(Integer sid : seatIds) {
                updStmt.setInt(1, userId); updStmt.setInt(2, bookingId); updStmt.setInt(3, sid); updStmt.addBatch();
            }
            updStmt.executeBatch(); conn.commit(); return true;
        } catch(Exception e) { if(conn != null) conn.rollback(); throw e; }
        finally { if(conn != null) conn.close(); }
    }
    
    public void cancelBooking(int bookingId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            String sql1 = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
            String sql2 = "UPDATE seats SET is_booked = false, booked_by_user_id = NULL, booking_id = NULL WHERE booking_id = ?";
            try(PreparedStatement p1 = conn.prepareStatement(sql1);
                PreparedStatement p2 = conn.prepareStatement(sql2)) {
                p1.setInt(1, bookingId); p1.executeUpdate();
                p2.setInt(1, bookingId); p2.executeUpdate();
                conn.commit();
            } catch(Exception e) { conn.rollback(); throw e; }
        }
    }
    
    public void generateSeatsForShow(int showId) throws SQLException {
        String chk = "SELECT COUNT(*) FROM seats WHERE show_id = ?";
        try (Connection conn = DBConfig.getConnection(); PreparedStatement cp = conn.prepareStatement(chk)) {
            cp.setInt(1, showId); ResultSet rs = cp.executeQuery();
            if(rs.next() && rs.getInt(1) > 0) return;
        }
        String sql = "INSERT INTO seats (show_id, row_label, seat_number, is_booked) VALUES (?, ?, ?, false)";
        try (Connection conn = DBConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (char row = 'A'; row <= 'J'; row++) {
                for (int num = 1; num <= 10; num++) {
                    pstmt.setInt(1, showId); pstmt.setString(2, String.valueOf(row)); pstmt.setInt(3, num); pstmt.addBatch();
                }
            }
            pstmt.executeBatch(); conn.commit();
        }
    }
    
    public List<Seat> getSeatsForShow(int showId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT * FROM seats WHERE show_id = ? ORDER BY row_label, seat_number";
        try (Connection conn = DBConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, showId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seats.add(new Seat(rs.getInt("id"), rs.getInt("show_id"), rs.getString("row_label").charAt(0), rs.getInt("seat_number"), rs.getBoolean("is_booked")));
            }
        }
        return seats;
    }
}