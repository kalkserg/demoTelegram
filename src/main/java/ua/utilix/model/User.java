package ua.utilix.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private Long chatId;
    private String sigfoxName = "";
    private String sigfoxId = "";
    private Boolean admin;
    private Boolean notified = false;

    public User() {
    }

    public User(Long chatId, String sigfoxName, String sigfoxId) {
        this.chatId = chatId;
        this.sigfoxId = sigfoxId;
        this.sigfoxName = sigfoxName;
    }

    public User(Long chatId, Boolean admin) {
        this.chatId = chatId;
        this.admin = admin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSigfoxName() {
        return sigfoxName;
    }

    public void setSigfoxName(String sigfoxName) {
        this.sigfoxName = sigfoxName;
    }
    public String getSigfoxId() {
        return sigfoxId;
    }

    public void setSigfoxId(String sigfoxId) {
        this.sigfoxId = sigfoxId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }


    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }
}
