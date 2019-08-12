package com.example.mymessages.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.mymessages.adapter.ContactsAdapter;
import com.example.mymessages.adapter.GroupSelectedAdapter;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.RecyclerItemClickListener;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.example.mymessages.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private RecyclerView recyclerSelectedMembers, recyclerMembers;
    private ContactsAdapter contactsAdapter;
    private GroupSelectedAdapter groupSelectedAdapter;
    private List<User> listMembers = new ArrayList<>();
    private List<User> listSelectedMembers = new ArrayList<>();
    private ValueEventListener valueEventListenerMembers;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private Toolbar toolbar;
    private FloatingActionButton fabNextRegister;

    public void updateMembersToolbar(){
        int totalSelectedMembers = listSelectedMembers.size();
        int totalMembers = listMembers.size() + totalSelectedMembers;

        toolbar.setSubtitle(totalSelectedMembers + " de " + totalMembers + " selecionados");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        setSupportActionBar(toolbar);

       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //configuracoes iniciais
        recyclerMembers = findViewById(R.id.recyclerMembers);
        recyclerSelectedMembers = findViewById(R.id.recyclerSelectedMembers);
        usersRef = ConfigFirebase.getFirebaseDatabase().child("users");
        currentUser = UsuarioFirebase.getUsuarioAtual(); //usuario atual para remover dos contatos
        fabNextRegister = findViewById(R.id.fabNextGroup);

        //configurar adapter
        contactsAdapter = new ContactsAdapter(listMembers, getApplicationContext());

        //configurar recycler
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembers.setLayoutManager(layoutManager);
        recyclerMembers.setHasFixedSize(true);
        recyclerMembers.setAdapter(contactsAdapter);

        //evento de clique
        recyclerMembers.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembers,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                User selectedUser = listMembers.get(position);

                                //remover usuario selecionado da lista de membros
                                listMembers.remove(selectedUser);
                                contactsAdapter.notifyDataSetChanged();

                                //adiciona usuario na lista de membros selecionados
                                listSelectedMembers.add(selectedUser);
                                groupSelectedAdapter.notifyDataSetChanged();

                                updateMembersToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );

        //configurar recycler view de membros selecionados
        groupSelectedAdapter = new GroupSelectedAdapter(listSelectedMembers, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false
        );
        recyclerSelectedMembers.setLayoutManager(layoutManagerHorizontal);
        recyclerSelectedMembers.setHasFixedSize(true);
        recyclerSelectedMembers.setAdapter(groupSelectedAdapter);

        //evento de clique membros selecionados
        recyclerSelectedMembers.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerSelectedMembers,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                User selectedUser = listSelectedMembers.get(position);

                                //remover da listagem de membros selecionados
                                listSelectedMembers.remove(selectedUser);
                                groupSelectedAdapter.notifyDataSetChanged();

                                //adicionar à listagem de membros gerais novamente
                                listMembers.add(selectedUser);
                                contactsAdapter.notifyDataSetChanged();

                                updateMembersToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );

        //configurar floating action button
        fabNextRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupActivity.this, RegisterGroupActivity.class);
                i.putExtra("members", (Serializable)listSelectedMembers);
                startActivity(i);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        listMembers.clear();
        returnContacts();
    }

    @Override
    public void onStop() {
        super.onStop();
        usersRef.removeEventListener(valueEventListenerMembers);
    }

    //retorna contatos e adiciona na lista de contatos
    public void returnContacts(){

        valueEventListenerMembers = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    User user = data.getValue(User.class);
                    //só adicionar se o usuario nao for o usuario atual
                    if(!currentUser.getEmail().equals(user.getEmail())){
                        listMembers.add(user);
                    }
                }

                contactsAdapter.notifyDataSetChanged();
                updateMembersToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
