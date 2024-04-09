package com.socialmedia.socialmedia.models;

import com.google.firebase.auth.ActionCodeResult;

public class ModelUser {
    //use same name as in firebase database
    String name, email, bio, search, phone, image, cover, uid, onlineStatus, typingTo, UserNeighborhood, isBusinessOwner;
    boolean isBlocked = false;

    public ModelUser() {

    }

    public ModelUser(String name, String email, String bio, String search, String phone, String image, String cover, String uid, String onlineStatus, String typingTo, String UserNeighborhood, String isBusinessOwner, boolean isBlocked) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.UserNeighborhood = UserNeighborhood;
        this.isBusinessOwner = isBusinessOwner;
        this.isBlocked = isBlocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {this.bio = bio;}

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String geUserNeighborhood() {
        return UserNeighborhood;
    }

    public void setUserNeighborhood(String UserNeighborhood) {
        this.UserNeighborhood = UserNeighborhood;
    }

    public String getisBusinessOwner() {
        return isBusinessOwner;
    }

    public void setisBusinessOwner(String isBusinessOwner) {
        this.isBusinessOwner = isBusinessOwner;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
