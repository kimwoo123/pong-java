package com.example.board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserInfo {

    @Id
    private Integer id;

    private String login;
    private String email;
    private String image;
    private boolean needOtp;
    private boolean isVerified;

    // 기본 생성자
    public UserInfo() {
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public boolean getNeedOtp() {
        return needOtp;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setNeedOtp(boolean needOtp) {
        this.needOtp = needOtp;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }
}
