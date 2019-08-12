package com.example.mymessages.model;

import com.example.mymessages.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;

public class Conversation {

    private String idSender;
    private String idRecipient;
    private String lastMessage;
    private User displayedUser;
    private String isGroup;
    private Group group;

    public Conversation() {
        this.setIsGroup("false");
    }

    public void save(){
        DatabaseReference database = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference conversationRef = database.child("conversations");

        conversationRef.child(this.getIdSender())
                .child(this.getIdRecipient()).setValue(this);
    }

    public String getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(String isGroup) {
        this.isGroup = isGroup;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdRecipient() {
        return idRecipient;
    }

    public void setIdRecipient(String idRecipient) {
        this.idRecipient = idRecipient;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public User getDisplayedUser() {
        return displayedUser;
    }

    public void setDisplayedUser(User displayedUser) {
        this.displayedUser = displayedUser;
    }
}
