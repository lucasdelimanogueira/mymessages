package com.example.mymessages.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UsuarioFirebase {

    public static String getUserId(){
        FirebaseAuth user = ConfigFirebase.getFirebaseAuth();
        String email = user.getCurrentUser().getEmail();
        String userId = Base64Custom.codeBase64(email);

        return userId;
    }

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth user = ConfigFirebase.getFirebaseAuth();
        return user.getCurrentUser();
    }

    public static boolean updateUserPhoto(Uri url){
        try{
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar foto de perfil");
                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserName(String nome){
        try{
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar nome de perfil");
                    }
                }
            });
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static User getLoggedUserData(){
        FirebaseUser firebaseUser = getUsuarioAtual();
        User user = new User();
        user.setEmail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());

        if(firebaseUser.getPhotoUrl() == null){
            user.setPhoto("");
        }else{
            user.setPhoto(firebaseUser.getPhotoUrl().toString());
        }

        return user;
    }
}
