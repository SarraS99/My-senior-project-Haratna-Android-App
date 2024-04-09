package com.socialmedia.socialmedia.models;

public class ModelMenu {
    String mId;
    String mDesc;
    String mTitle;
    String mImage;
    String mEmail;
    String mName;
    String uid;
    String mPrice;

    public ModelMenu() {
    }

    public ModelMenu(String sId, String sDesc, String sTitle, String sImage, String sEmail, String sName, String uid, String sPrice) {
        this.mId = sId;
        this.mDesc = sDesc;
        this.mTitle = sTitle;
        this.mImage = sImage;
        this.mEmail = sEmail;
        this.mName = sName;
        this.uid = uid;
        this.mPrice = sPrice;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String sId) {
        this.mId = sId;
    }

    public String getmDesc() {
        return mDesc;
    }

    public void setmDesc(String sDesc) {
        this.mDesc = sDesc;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String sTitle) {
        this.mTitle = sTitle;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String sImage) {
        this.mImage = sImage;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String sEmail) {
        this.mEmail = sEmail;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String sName) {
        this.mName = sName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getmPrice() {
        return mPrice;
    }

    public void setmPrice(String sPrice) {
        this.mPrice = sPrice;
    }
}