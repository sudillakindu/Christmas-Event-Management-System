package org.event.chems;

public class MemberTable {
    private String chemsId;
    private String userType;
    private String nibmId;
    private String encryptedNibmId;
    private String memberName;
    private String batchNumber;
    private String email;
    private String payment;
    private String discount;
    private String price;

    public MemberTable(String chemsId, String userType, String nibmId, String encryptedNibmId, String memberName, String batchNumber,
                       String email, String payment, String discount, String price) {
        this.chemsId = chemsId;
        this.userType = userType;
        this.nibmId = nibmId;
        this.encryptedNibmId = encryptedNibmId ;
        this.memberName = memberName;
        this.batchNumber = batchNumber;
        this.email = email;
        this.payment = payment;
        this.discount = discount;
        this.price = price;

    }

    public void setChemsId(String chemsId) {
        this.chemsId = chemsId;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public void setNibmId(String nibmId) {
        this.nibmId = nibmId;
    }
    public void setEncryptedNibmId(String encryptedNibmId) {
        this.encryptedNibmId = encryptedNibmId;
    }
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
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

    public String getChemsId() {
        return chemsId;
    }
    public String getUserType() {
        return userType;
    }
    public String getNibmId() {
        return nibmId;
    }
    public String getEncryptedNibmId() {
        return encryptedNibmId;
    }
    public String getMemberName() {
        return memberName;
    }
    public String getBatchNumber() {
        return batchNumber;
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
