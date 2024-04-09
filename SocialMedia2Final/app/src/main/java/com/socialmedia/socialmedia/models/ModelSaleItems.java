package com.socialmedia.socialmedia.models;

public class ModelSaleItems {
    String sId;
    String sDesc;
    String sTitle;
    String sImage;
    String sEmail;
    String sName;
    String uid;
    String sPrice;
    String sCategory;

    public ModelSaleItems() {
    }

    public ModelSaleItems(String sId, String sDesc, String sTitle, String sImage, String sEmail, String sName, String uid, String sPrice) {
        this.sId = sId;
        this.sDesc = sDesc;
        this.sTitle = sTitle;
        this.sImage = sImage;
        this.sEmail = sEmail;
        this.sName = sName;
        this.uid = uid;
        this.sPrice = sPrice;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getsDesc() {
        return sDesc;
    }

    public void setsDesc(String sDesc) {
        this.sDesc = sDesc;
    }

    public String getsTitle() {
        return sTitle;
    }

    public void setsTitle(String sTitle) {
        this.sTitle = sTitle;
    }

    public String getsImage() {
        return sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getsPrice() {
        return sPrice;
    }

    public void setsPrice(String sPrice) {
        this.sPrice = sPrice;
    }

    public String getsCategory() {
        return sCategory;
    }

    public void setsCategory(String sCategory) {
        this.sCategory = sCategory;
    }
}
