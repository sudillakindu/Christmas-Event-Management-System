package org.event.chems;

public class Member {

    private String chems_id;
    private String user_type;
    private String nibm_id;
    private String encryptedNibm_id;
    private String member_name;
    private String batch_number;
    private String email;
    private String payment;
    private String discount;
    private String price;

    public Member(String chems_id, String user_type, String nibm_id, String encryptedNibm_id, String member_name, String batch_number, String email, String payment, String discount, String price) {
        this.chems_id = chems_id;
        this.user_type = user_type;
        this.nibm_id = nibm_id;
        this.encryptedNibm_id = encryptedNibm_id ;
        this.member_name = member_name;
        this.batch_number = batch_number;
        this.email = email;
        this.payment = payment;
        this.discount = discount;
        this.price = price;
    }

    public void setChems_id(String chems_id) {
        this.chems_id = chems_id;
    }
    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }
    public void setNibm_id(String nibm_id) {
        this.nibm_id = nibm_id;
    }
    public void setEncryptedNibm_id(String encryptedNibm_id) {
        this.encryptedNibm_id = encryptedNibm_id;
    }
    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }
    public void setBatch_number(String batch_number) {
        this.batch_number = batch_number;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPayment(String payment) {
        this.payment = payment;
    }
    public void setDiscount(String discount) {
        this.discount = discount;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getChems_id() {
        return chems_id;
    }
    public String getUser_type() {
        return user_type;
    }
    public String getNibm_id() {
        return nibm_id;
    }
    public String getEncryptedNibm_id() {
        return encryptedNibm_id;
    }
    public String getMember_name() {
        return member_name;
    }
    public String getBatch_number() {
        return batch_number;
    }
    public String getEmail() {
        return email;
    }
    public String getPayment() {
        return payment;
    }
    public String getDiscount() {
        return discount;
    }
    public String getPrice() {
        return price;
    }

}

