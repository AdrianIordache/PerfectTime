package com.example.adi.perfecttime;

public class Messages
{
    public String Date, From, Message, Time, Type;

    public  Messages()
    {
        Date = "Unknown";
        From = "Unknown";
        Message = "Unknown";
        Time = "Unknown";
        Type = "Unknown";
    }

    public Messages(String date, String from, String message, String time, String type) {
        Date = date;
        From = from;
        Message = message;
        Time = time;
        Type = type;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
