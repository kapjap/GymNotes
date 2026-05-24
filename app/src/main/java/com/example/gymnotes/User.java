package com.example.gymnotes;

public class User {
    private  String id;
    private  String name;
    private  String lastName;
    private  String email;

    public String getEmail() {
        return email;
    }

    public  User(String id, String name, String lastName, String email)
    {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }
}