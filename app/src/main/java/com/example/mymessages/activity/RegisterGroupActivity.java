package com.example.mymessages.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.mymessages.adapter.GroupSelectedAdapter;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.Group;
import com.example.mymessages.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymessages.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterGroupActivity extends AppCompatActivity {

    private List<User> listSelectedMembers = new ArrayList<>();
    private TextView textTotalMembers;
    private GroupSelectedAdapter groupSelectedAdapter;
    private RecyclerView recyclerSelectedMembers;
    private CircleImageView imageGroup;
    private static final int SELECT_GALLERY = 200;
    private StorageReference storageReference;
    private Group group;
    private FloatingActionButton fabSaveGroup;
    private EditText editGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //configurações iniciais
        textTotalMembers = findViewById(R.id.textTotalMembers);
        recyclerSelectedMembers = findViewById(R.id.recyclerGroupMembers);
        imageGroup = findViewById(R.id.imageGroup);
        storageReference = ConfigFirebase.getFirebaseStorage();
        fabSaveGroup = findViewById(R.id.fabSaveGroup);
        editGroupName = findViewById(R.id.editGroupName);
        group = new Group();

        //evento de clique imagem
        imageGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){//testa se é possivel abrir a galeria
                    startActivityForResult(i, SELECT_GALLERY);
                }

            }
        });

        //recupera lista de membros selecionados passada
        if(getIntent().getExtras() != null){
            List<User> members = (List<User>) getIntent().getExtras().getSerializable("members");
            listSelectedMembers.addAll(members);

            textTotalMembers.setText("Participantes: " + listSelectedMembers.size());
        }

        //configurar recyclerview
        groupSelectedAdapter = new GroupSelectedAdapter(listSelectedMembers, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false
        );
        recyclerSelectedMembers.setLayoutManager(layoutManagerHorizontal);
        recyclerSelectedMembers.setHasFixedSize(true);
        recyclerSelectedMembers.setAdapter(groupSelectedAdapter);

        //configurar floating action button salvar grupo
        fabSaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = editGroupName.getText().toString();

                //adicionar usuario atual na lista de membros
                listSelectedMembers.add(UsuarioFirebase.getLoggedUserData());

                //adicionar lista de membros aos dados do grupo
                group.setMembers(listSelectedMembers);

                //nome do grupo
                group.setName(groupName);

                //salvar dados no firebase
                group.save();

                //abrir chat grupo
                Intent i = new Intent(RegisterGroupActivity.this, ChatActivity.class);
                i.putExtra("chatGroup", group);
                startActivity(i);


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap image = null;

            try{
                Uri selectedImageLocal = data.getData();
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageLocal);

                if(image != null){
                    imageGroup.setImageBitmap(image);

                    //recuperar dados da imagem para firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    //salvar imagem no firebase
                    final StorageReference imageRef = storageReference
                            .child("images")
                            .child("groups")
                            .child(group.getId() + ".jpeg");

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterGroupActivity.this,
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

                                    String urlString = url.toString();
                                    group.setPhoto(urlString);

                                    Toast.makeText(RegisterGroupActivity.this,
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
}
