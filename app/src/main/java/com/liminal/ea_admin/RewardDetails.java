package com.liminal.ea_admin;

public class RewardDetails {
    String rid;
    String title;
    String description;
    long cost;
    long quantity;

    public RewardDetails(String rid, String title, String description, long cost, long quantity){
        this.rid = rid;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.quantity = quantity;
    }

    public String getRid(){
        return rid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getCost(){
        return cost;
    }

    public long getQuantity() {
        return quantity;
    }
}
