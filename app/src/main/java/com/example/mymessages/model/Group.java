package com.example.mymessages.model;

import android.provider.ContactsContract;

import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.Base64Custom;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {

    private String id;
    private String name;
    private String photo;
    private List<User> members;

    public Group() {
        DatabaseReference database = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference groupRef = database.child("groups");

        String groupIdFirebase = groupRef.push().getKey();
        setId(groupIdFirebase);
    }

    public void save(){
        DatabaseReference database = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference groupRef = database.child("groups");

        groupRef.child(getId()).setValue(this);

        //salvar conversa para cada membro do grupo
        for(User member:getMembers()){

            String idSender = Base64Custom.codeBase64(member.getEmail());

            //o destinatario é o proprio grupo
            String idRecipient = getId();

            Conversation conversation = new Conversation();
            conversation.setIdSender(idSender);
            conversation.setIdRecipient(idRecipient);
            conversation.setLastMessage("");
            conversation.setIsGroup("true");
            conversation.setGroup(this);

            conversation.save();
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }
}
