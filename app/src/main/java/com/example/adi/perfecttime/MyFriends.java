package com.example.adi.perfecttime;

public class MyFriends
{
    public String Date;
    public String profileImage, fullName, status, userID, state;

    public MyFriends()
    {
        this.Date = "Unknown";
        this.profileImage = "Unknown";
        this.fullName = "Unknown";
        this.status = "Unknown";
        this.userID = "Unknown";
        this.state = "Unknown";
    }

    public MyFriends(String profileImage, String fullName, String status, String userID, String date, String State) {
        this.Date = date;
        this.profileImage = profileImage;
        this.fullName = fullName;
        this.status = status;
        this.userID = userID;
        this.state = State;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
