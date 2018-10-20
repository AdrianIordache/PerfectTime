package com.example.adi.perfecttime;

public class Friends
{
    public String profileImage, fullName, status, userID;

    public Friends()
    {
        this.profileImage = "Unknown";
        this.fullName = "Unknown";
        this.status = "Unknown";
        this.userID = "Unknown";
    }

    public Friends(String profileImage, String fullName, String status, String userID) {
        this.profileImage = profileImage;
        this.fullName = fullName;
        this.status = status;
        this.userID = userID;
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
}
