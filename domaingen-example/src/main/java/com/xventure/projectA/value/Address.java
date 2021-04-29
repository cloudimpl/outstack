package com.xventure.projectA.value;


public class Address {
    private final String streetNo ;
    private final int zipCode ;
    private final int postalCode ;

    public Address(String streetNo, int zipCode, int postalCode) {
        this.streetNo = streetNo ;
        this.zipCode = zipCode ;
        this.postalCode = postalCode ;
    }

    public String getStreetNo() {
        return this.streetNo ;
    }

    public int getZipCode() {
        return this.zipCode ;
    }

    public int getPostalCode() {
        return this.postalCode ;
    }
}
