package com.example.hotelboulevard;

public class Reservation {
    private String userName;
    private String roomName;
    private String checkIn;
    private String checkOut;
    private String price;
    private int imageId;

    public Reservation(String userName, String roomName, String checkIn, String checkOut, String price, int imageId) {
        this.userName = userName;
        this.roomName = roomName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.price = price;
        this.imageId = imageId;
    }

    public String getUserName() { return userName; }
    public String getRoomName() { return roomName; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public String getPrice() { return price; }
    public int getImageId() { return imageId; }
}
