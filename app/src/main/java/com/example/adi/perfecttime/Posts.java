package com.example.adi.perfecttime;

import android.support.annotation.NonNull;
import android.widget.SimpleCursorTreeAdapter;

import java.util.Comparator;

public class Posts
{
    public String Date, FullName, Image, PostDescription, ProfileImage, Time, UserID;
    public long Counter;

    public Posts(){
        Date = "Unknown";
        FullName = "Unknown";
        Image = "Unknown";
        PostDescription = "Unknown";
        ProfileImage = "Unknown";
        Time = "Unknown";
        UserID = "Unknown";
        Counter = 0;
    }

    public Posts(String date, String fullName, String image, String postDescription, String profileImage, String time, String userID, long counter) {
        Date = date;
        FullName = fullName;
        Image = image;
        PostDescription = postDescription;
        ProfileImage = profileImage;
        Time = time;
        UserID = userID;
        Counter = counter;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getPostDescription() {
        return PostDescription;
    }

    public void setPostDescription(String postDescription) {
        PostDescription = postDescription;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public long getCounter() {
        return Counter;
    }

    public void setCounter(long counter) {
        Counter = counter;
    }

    public boolean equals(Object o)
    {
        if(o == this)
        {
            return true;
        }

        if(!(o instanceof Posts))
        {
            return false;
        }

        Posts obj = (Posts) o;

        if(Date.compareTo(obj.Date) == 0 && FullName.compareTo(obj.FullName) == 0 && Time.compareTo(obj.Time) == 0 && UserID.compareTo(obj.UserID) == 0
                && PostDescription.compareTo(obj.PostDescription) == 0  && ProfileImage.compareTo(obj.ProfileImage) == 0 && Image.compareTo(obj.Image) == 0 && Counter == obj.Counter)
        {
            return true;
        }
        else {
            return false;
        }
    }
}

class SortPosts implements Comparator<Posts>
{
    public int compare(Posts a, Posts b)
    {
        return (int) (a.Counter - b.Counter);
    }
}
