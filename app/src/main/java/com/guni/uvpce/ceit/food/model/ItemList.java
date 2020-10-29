package com.guni.uvpce.ceit.food.model;

public class ItemList {
    public String category, item_name, price, item_image, status;

    public ItemList(String category, String item_name, String price, String item_image, String status) {
        this.category = category;
        this.item_name = item_name;
        this.price = price;
        this.item_image = item_image;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ItemList() {
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

    public String getItem_image() {
        return item_image;
    }

    public void setItem_image(String item_image) {
        this.item_image = item_image;
    }
}
