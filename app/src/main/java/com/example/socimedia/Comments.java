package com.example.socimedia;

public class Comments
{

    public  String userName,comment,date,time;

    public Comments()
    {

    }

    public Comments(String userName, String comment, String date, String time) {
        this.userName = userName;
        this.comment = comment;
        this.date = date;
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
