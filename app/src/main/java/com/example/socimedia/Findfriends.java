package com.example.socimedia;
public class Findfriends
{
    public String profileimage,fullName,status;

    public Findfriends()
    {
        //default constructor//
    }

    //parameterized constructor//
    public Findfriends(String profileimage, String fullName, String status) {
        this.profileimage = profileimage;
        this.fullName = fullName;
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
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
}
