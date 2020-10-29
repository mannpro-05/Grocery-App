package com.guni.uvpce.ceit.food.model;

public class CartItemList {
    public String category, item_name, price, total, number, item_image, date, time, uid, order_status;

    public CartItemList(String category, String item_name, String price, String total, String number, String item_image, String date, String time, String uid, String order_status) {
        this.category = category;
        this.item_name = item_name;
        this.price = price;
        this.total = total;
        this.number = number;
        this.item_image = item_image;
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.order_status = order_status;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CartItemList() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getItem_image() {
        return item_image;
    }

    public void setItem_image(String item_image) {
        this.item_image = item_image;
    }
}
