package com.example.mymessages.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.mymessages.adapter.MessagesAdapter;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.Base64Custom;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.Conversation;
import com.example.mymessages.model.Group;
import com.example.mymessages.model.Message;
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
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymessages.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewName;
    private CircleImageView circleImageViewPhoto;
    private User recipientUser;
    private User senderUser;
    private EditText editMessage;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference msgRef;
    private ChildEventListener childEventListenerMessages;
    private ImageView imageCameraChat;
    private static final int SELECT_CAMERA = 100;

    //id usuario rementente e destinatario
    private String idSenderUser;
    private String idRecipientUser;
    private Group group;

    private RecyclerView recyclerMessages;
    private MessagesAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configuracoes iniciais
        textViewName = findViewById(R.id.textViewChatName);
        circleImageViewPhoto = findViewById(R.id.imageViewContactPhoto);
        editMessage = findViewById(R.id.editMessage);
        recyclerMessages = findViewById(R.id.recycleMessages);
        imageCameraChat = findViewById(R.id.imageCameraChat);

        //recupera os dados do usuário rementente
        idSenderUser = UsuarioFirebase.getUserId();
        senderUser = UsuarioFirebase.getLoggedUserData();

        //recuperar dados do usuario destinatário
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            //verifica se é grupo
            if(bundle.containsKey("chatGroup")){//conversa de grupo

                /***** conversa de grupo *****/
                group = (Group)bundle.getSerializable("chatGroup");
                //recuperar os dados do usuário destinatário (que é o id do grupo)
                idRecipientUser = group.getId();

                //nome do grupo
                textViewName.setText(group.getName());

                //recuperar foto do grupo e colocar no circleimageview da tela de chat
                /*String photo = group.getPhoto();
                if(photo != null){
                    Uri uri = Uri.parse(group.getPhoto());
                    Glide.with(ChatActivity.this).load(uri).into(circleImageViewPhoto);
                }else{
                    circleImageViewPhoto.setImageResource(R.drawable.padrao);
                }*/
                //circleImageViewPhoto.setImageResource(R.drawable.padrao);

                /****************************/

            }else{//conversa normal

                /***** conversa normal ****/
                recipientUser = (User)bundle.getSerializable("chatContact");
                textViewName.setText(recipientUser.getName());

                //recuperar foto do usuário e colocar no circleimageview da tela de chat
                /*String photo = recipientUser.getPhoto();
                if(photo != null){
                    Uri uri = Uri.parse(recipientUser.getPhoto());
                    //Glide.with(ChatActivity.this).load(uri).into(circleImageViewPhoto);
                }else{
                    circleImageViewPhoto.setImageResource(R.drawable.padrao);
                }*/
                //circleImageViewPhoto.setImageResource(R.drawable.padrao);

                //recuperar os dados do usuário destinatário
                idRecipientUser = Base64Custom.codeBase64(recipientUser.getEmail());
                /****************************/
            }
        }

        //configurar recycler view mensagens
        //configuracao do adapter
        adapter = new MessagesAdapter(messages, getApplicationContext());

        //configuracao do recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setHasFixedSize(true);
        recyclerMessages.setAdapter(adapter);

        database = ConfigFirebase.getFirebaseDatabase();
        storage = ConfigFirebase.getFirebaseStorage();
        msgRef = database.child("messages")
                .child(idSenderUser)
                .child(idRecipientUser);

        //evento de clique na camera
        imageCameraChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null){//testa se é possivel abrir a camera
                    startActivityForResult(i, SELECT_CAMERA);
                }
            }
        });
    }

    //ao clicar na camera e tirar foto resultado do que foi feito (foto tirada)


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap image = null;

            try{
                switch(requestCode){
                    case SELECT_CAMERA:
                        image = (Bitmap)data.getExtras().get("data");
                        break;
                }

                if(image != null){
                    //recuperar dados da imagem para firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    //criar nome da imagem
                    String imageName = UUID.randomUUID().toString();

                    //configurar referencia do firebase para salvar a imagem no local correto
                    StorageReference imageRef = storage.child("images")
                            .child("photos")
                            .child(idSenderUser)
                            .child(imageName);

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ChatActivity.this,
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
                                    //recuperar url da imagem upada no firebase
                                    String downloadUrl = url.toString();

                                    //salvar url da imagem na mensagem
                                    Message message = new Message();
                                    message.setUserId(idSenderUser);
                                    message.setMessage("image.jpeg");
                                    message.setImage(downloadUrl);

                                    //salvar mensagem no firebase para rementente
                                    saveMessage(idSenderUser, idRecipientUser, message);

                                    //salvar mensagem no firebase para destinatario
                                    saveMessage(idRecipientUser, idSenderUser, message);

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

    public void saveConversation(String idSender, String idRecipient, User displayedUser, Message msg, boolean isGroup){

        //salvar conversa remetente
        Conversation conversationSender = new Conversation();
        conversationSender.setIdSender(idSender);
        conversationSender.setIdRecipient(idRecipient);
        conversationSender.setLastMessage(msg.getMessage());

        if(isGroup){//conversa de grupo

            conversationSender.setIsGroup("true");
            conversationSender.setGroup(group);

        }else{//conversa normal

            conversationSender.setDisplayedUser(displayedUser);
            conversationSender.setIsGroup("false");
        }
        //salvar conversa no firebase
        conversationSender.save();
    }

    public void sendMessage(View view){
        String textMessage = editMessage.getText().toString();

        if(!textMessage.isEmpty()){

            //verificar se é grupo
            if(recipientUser != null){//conversa entre duas pessoas

                Message message = new Message();
                message.setUserId(idSenderUser);
                message.setMessage(textMessage);

                //salvar mensagem para o remetente
                saveMessage(idSenderUser, idRecipientUser, message);

                //salvar mensagem para o destinatario
                saveMessage(idRecipientUser, idSenderUser, message);

                //salvar no de conversa para listar no recyclerview (remetente)
                saveConversation(idSenderUser, idRecipientUser, recipientUser, message, false);

                //salvar no de conversa para listar no recyclerview (destinatário)
                saveConversation(idRecipientUser, idSenderUser, senderUser, message, false);

                //limpar texto da caixa
                editMessage.setText("");

            }else{//grupo
                final List<User> listUser = new ArrayList<>();
                DatabaseReference database = ConfigFirebase.getFirebaseDatabase();
                DatabaseReference memberRef = database.child("users");
                memberRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                            User user = postSnapshot.getValue(User.class);
                                listUser.add(user);

                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                for(User member: listUser){
                    String idGroupSender = Base64Custom.codeBase64(member.getEmail());
                    String idLoggerUser = UsuarioFirebase.getUserId();

                    Message message = new Message();
                    message.setUserId(idLoggerUser);
                    message.setMessage(textMessage);
                    message.setName(senderUser.getName());

                    //salvar mensagem para cada membro
                    //no caso do grupo, o idRecipientUser é o id do próprio grupo
                    saveMessage(idGroupSender, idRecipientUser, message);

                    //salvar conversa para listar no recyclerview (remetente)
                    saveConversation(idGroupSender, idRecipientUser, recipientUser, message, true);
                }

            }
            //limpar texto da caixa
            editMessage.setText("");
        }
    }

    private void saveMessage(String idSender, String idRecipient, Message msg){
        DatabaseReference database = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference msgRef = database.child("messages");

        msgRef.child(idSender).child(idRecipient).push().setValue(msg);

        //limpar caixa de texto
        editMessage.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        messages.clear();
        returnMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        msgRef.removeEventListener(childEventListenerMessages);
    }

    private void returnMessages(){
        childEventListenerMessages = msgRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
