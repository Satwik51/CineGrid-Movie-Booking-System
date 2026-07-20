package com.cinegrid.model;

public class Customer extends User {
    public Customer(int id, String name, String email) {
        super(id, name, email, "CUSTOMER");
    }
}