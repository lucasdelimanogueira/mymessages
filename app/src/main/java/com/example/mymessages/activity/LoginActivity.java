 package com.example.mymessages.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mymessages.R;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

 public class LoginActivity extends AppCompatActivity {

     private TextInputEditText fieldEmail, fieldPassword;
     private FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authentication = ConfigFirebase.getFirebaseAuth();

        fieldEmail = findViewById(R.id.editLoginEmail);
        fieldPassword = findViewById(R.id.editLoginPassword);
    }

    public void signInUser(User user){
        authentication.signInWithEmailAndPassword(
                user.getEmail(), user.getPassword()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){//se autenticacao foi bem sucedida

                    //abre tela principal
                    openMainActivity();

                }else{//nao foi possivel autenticar usuario
                    String exception = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        exception = "Usuário não cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        exception = "E-mail ou senha incorretos";
                    }catch (Exception e){
                        exception = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    //mensagem na tela
                    Toast.makeText(LoginActivity.this, exception, Toast.LENGTH_SHORT).show();
                }//fim do else
            }
        });
    }

    public void validateAuthUser(View view){
        //recuperar dados usuário
        String textEmail = fieldEmail.getText().toString();
        String textPassword = fieldPassword.getText().toString();

        //validar email e senha
            if(!textEmail.isEmpty()){
                if(!textPassword.isEmpty()){
                    //todos campos preenchidos

                    //instancia usuario
                    User user = new User();
                    user.setEmail(textEmail);
                    user.setPassword(textPassword);

                    //logar usuario
                    signInUser(user);

                }else{//campo senha nao preenchida
                    Toast.makeText(LoginActivity.this, "Digite uma senha", Toast.LENGTH_SHORT).show();
                }
            }else{//campo email não preenchido
                Toast.makeText(LoginActivity.this, "Digite seu e-mail", Toast.LENGTH_SHORT).show();
            }
        }

     @Override
     protected void onStart() {
         super.onStart();
         FirebaseUser currentUser = authentication.getCurrentUser();
         if(currentUser != null){
             openMainActivity();
         }
     }

     //abre tela de cadastro
    public void openRegisterActivity(View view){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void openMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
