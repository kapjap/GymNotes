package com.example.gymnotes;

public class Product {

    public Integer id;
    public String title;
    public double price;
    public String category;
    public String description;

    public Product() {}

    public Product(Integer id,String title, double price, String category, String description) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.category = category;
        this.description = description;
    }
}
