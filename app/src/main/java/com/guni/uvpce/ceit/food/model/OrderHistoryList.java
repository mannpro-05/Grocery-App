package com.guni.uvpce.ceit.food.model;

public class OrderHistoryList {
    private String Total, date, time, uid;

    public OrderHistoryList(String Total, String date, String time, String uid) {
        this.Total = Total;
        this.date = date;
        this.time = time;
        this.uid = uid;
    }

    public OrderHistoryList() {
    }

    private String getTotal() {
        return Total;
    }

    public void setTotal(String Total) {
        this.Total = Total;
    }

    private String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String getTime() {
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
}
