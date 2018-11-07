package com.prototipo.prototipo.prototipo;

public class Customer {
    private String name;
    private String email;
    private String phone;

    public Customer(String name, String email, String phone){
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    @Override
    public String toString(){
        return name;
    }

}
