package com.example.adi.perfecttime;

public class Comments
{
    private String Time, Date, Username, UserID, Comment, ProfileImage;

    public Comments()
    {
        Time = "Unknown";
        Date = "Unknown";
        Username = "Unknown";
        UserID = "Unknown";
        Comment = "Unknown";
        ProfileImage = "Unknown";
    }

    public Comments(String time, String date, String username, String userID, String comment, String profileImage) {
        Time = time;
        Date = date;
        Username = username;
        UserID = userID;
        Comment = comment;
        ProfileImage = profileImage;
    }

    public String getTime() {
        return Time;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }
}
