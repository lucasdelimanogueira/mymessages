package com.example.mymessages.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mymessages.R;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.helper.Permission;
import com.example.mymessages.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfigActivity extends AppCompatActivity {

    private ImageButton imageButtonCamera, imageButtonGallery;
    private static final int SELECT_CAMERA = 100;
    private static final int SELECT_GALLERY = 200;
    private CircleImageView circleImageViewProfile;
    private EditText editProfileName;
    private ImageView imageUpdateName;
    private StorageReference storageReference;
    private String userId;
    private User loggedUser;

    public String[] requiredPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        //configuracoes iniciais
        storageReference = ConfigFirebase.getFirebaseStorage();
        userId = UsuarioFirebase.getUserId();
        loggedUser = UsuarioFirebase.getLoggedUserData();

        //validar permissoes de acesso a camera, galeria, etc
        Permission.validatePermissions(requiredPermissions, this, 1);

        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGallery = findViewById(R.id.imageButtonGallery);
        circleImageViewProfile = findViewById(R.id.profile_image);
        editProfileName = findViewById(R.id.editProfileName);
        imageUpdateName = findViewById(R.id.imageUpdateName);

        //recuperar dados do usuario
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        Uri url = user.getPhotoUrl();
        if(url != null) {
            Glide.with(ConfigActivity.this)
                    .load(url)
                    .into(circleImageViewProfile);
        }else{
            circleImageViewProfile.setImageResource(R.drawable.padrao);
        }

        //coloca o nome do usuario no edittext
        editProfileName.setText(user.getDisplayName());

        //criar toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //clicar no botao de abrir camera
        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null){//testa se é possivel abrir a camera
                    startActivityForResult(i, SELECT_CAMERA);
                }

            }
        });

        //clicar no botao de abrir galeria
        imageButtonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){//testa se é possivel abrir a galeria
                    startActivityForResult(i, SELECT_GALLERY);
                }
            }
        });

        //clicar no botao de atualizar nome
        imageUpdateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editProfileName.getText().toString();
                boolean retorno = UsuarioFirebase.updateUserName(name);

                if(retorno){

                    loggedUser.setName(name);
                    loggedUser.update();

                    Toast.makeText(ConfigActivity.this,
                            "Nome alterado com sucesso!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap image = null;

            try {

                switch(requestCode){
                    case SELECT_CAMERA:
                        image = (Bitmap)data.getExtras().get("data");
                        break;
                    case SELECT_GALLERY:
                        Uri selectedImageLocal = data.getData();
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageLocal);
                        break;
                }

                if(image != null){
                    circleImageViewProfile.setImageBitmap(image);

                    //recuperar dados da imagem para firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    //salvar imagem no firebase
                    final StorageReference imageRef = storageReference
                            .child("images")
                            .child("profile")
                            .child(userId + ".jpeg");

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfigActivity.this,
                                    "Erro ao salvar imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri url) {
                                    updateUserPhoto(url);

                                    Toast.makeText(ConfigActivity.this,
                                            "Sucesso ao salvar imagem",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void updateUserPhoto(Uri url){
        boolean retorno = UsuarioFirebase.updateUserPhoto(url); //atualiza foto no firebaseuser
        if(retorno){
            //atualiza foto no banco
            loggedUser.setPhoto(url.toString());
            loggedUser.update();

            Toast.makeText(ConfigActivity.this,
                    "Sua foto foi alterada!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //caso usuario recuse a permissão
        for(int permissionResult : grantResults){
            if(permissionResult == PackageManager.PERMISSION_DENIED){

            }
        }
    }

    private void permissionValidationAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o aplicativo corretamente é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
