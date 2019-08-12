package com.example.mymessages.model;

import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.UsuarioFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String password;
    private String photo;

    public User() {
    }

    public void save(){
        DatabaseReference firebaseRef = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference user = firebaseRef.child("users").child(getId());

        user.setValue(this);
    }

    public void update(){
        String userIdentification = UsuarioFirebase.getUserId();
        DatabaseReference database = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference usersRef = database.child("users")
                .child(userIdentification);
        Map<String, Object> userValues = convertToMap();
        usersRef.updateChildren(userValues);
    }

    @Exclude
    public Map<String, Object> convertToMap(){
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", getEmail());
        userMap.put("name", getName());
        userMap.put("photo", getPhoto());
        return userMap;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Exclude
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
