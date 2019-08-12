package com.example.mymessages.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mymessages.R;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.Base64Custom;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText fieldName, fieldEmail, fieldPassword;
    private FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fieldName = findViewById(R.id.editProfileName);
        fieldEmail = findViewById(R.id.editLoginEmail);
        fieldPassword = findViewById(R.id.editLoginPassword);
    }

    public void registerUser(final User user){
        authentication = ConfigFirebase.getFirebaseAuth();
        authentication.createUserWithEmailAndPassword(
            user.getEmail(), user.getPassword()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){//se cadastrado com sucesso

                    try{
                        String userId = Base64Custom.codeBase64(user.getEmail());
                        user.setId(userId);
                        user.save();
                        UsuarioFirebase.updateUserName(user.getName());

                        //mensagem na tela
                        Toast.makeText(RegisterActivity.this, "Sucesso ao cadastrar usuário",
                                Toast.LENGTH_SHORT).show();

                        finish();

                    }catch (Exception e){
                        e.printStackTrace();
                    }



                }else{//nao possivel cadastrar usuario
                    String exception = "";
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        exception = "Digite uma senha mais forte";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        exception = "Por favor, digite um e-mail válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        exception = "Conta já cadastrada";
                    }catch (Exception e){
                        exception = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    //mensagem na tela
                    Toast.makeText(RegisterActivity.this, exception, Toast.LENGTH_SHORT).show();
                }//fim do else

            }
        });
    }

    public void validateUserRegister(View view){

        //recueprar texto dos campos
        String textName = fieldName.getText().toString();
        String textEmail = fieldEmail.getText().toString();
        String textPassword = fieldPassword.getText().toString();

        if(!textName.isEmpty()){
            if(!textEmail.isEmpty()){
                if(!textPassword.isEmpty()){
                    //todos campos preenchidos

                    //instancia usuario
                    User user = new User();
                    user.setName(textName);
                    user.setEmail(textEmail);
                    user.setPassword(textPassword);

                    //registra usuario no firebase
                    registerUser(user);
                }else{//campo senha nao preenchida
                    Toast.makeText(RegisterActivity.this, "Digite uma senha", Toast.LENGTH_SHORT).show();
                }
            }else{//campo email não preenchido
                Toast.makeText(RegisterActivity.this, "Digite seu e-mail", Toast.LENGTH_SHORT).show();
            }
        }else{//campo nome nao preenchido
            Toast.makeText(RegisterActivity.this, "Digite seu nome", Toast.LENGTH_SHORT).show();
        }
    }
}
