package com.cinegrid.model;

public class Seat {
    private int id; private int showId; private char rowLabel; private int seatNumber; private boolean isBooked;
    public Seat(int id, int showId, char rowLabel, int seatNumber, boolean isBooked) {
        this.id = id; this.showId = showId; this.rowLabel = rowLabel; this.seatNumber = seatNumber; this.isBooked = isBooked;
    }
    public int getId() { return id; }
    public char getRowLabel() { return rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public boolean isBooked() { return isBooked; }
}