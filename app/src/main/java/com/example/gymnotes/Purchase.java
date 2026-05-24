package com.example.gymnotes;

public class Purchase {
    public String name;
    public String lastName;
    public String email;
    public String cardNumber;
    public Integer productId;

    public Purchase() {

    }

    public Purchase(String name, String lastName, String email, String cardNumber,Integer productId) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.cardNumber = cardNumber;
        this.productId = productId;
    }
}