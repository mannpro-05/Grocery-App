package com.guni.uvpce.ceit.food.model;

public class User {

    public String name, email, mobile_no, address, user_role;

    public User() {
    }

    public User(String name, String email, String mobile_no, String address, String user_role) {
        this.name = name;
        this.email = email;
        this.mobile_no = mobile_no;
        this.address = address;
        this.user_role = user_role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUser_role() {
        return user_role;
    }

    public void setUser_role(String user_role) {
        this.user_role = user_role;
    }
}
