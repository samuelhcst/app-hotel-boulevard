package com.example.hotelboulevard;

public class Room {
    private String name;
    private float rating;
    private String specs;
    private String price;
    private int imageId; // Drawable resource ID

    public Room(String name, float rating, String specs, String price, int imageId) {
        this.name = name;
        this.rating = rating;
        this.specs = specs;
        this.price = price;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
