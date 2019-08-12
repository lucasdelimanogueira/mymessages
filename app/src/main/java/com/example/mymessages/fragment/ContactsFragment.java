package com.example.mymessages.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.example.mymessages.R;
import com.example.mymessages.activity.ChatActivity;
import com.example.mymessages.activity.GroupActivity;
import com.example.mymessages.adapter.ChatAdapter;
import com.example.mymessages.adapter.ContactsAdapter;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.RecyclerItemClickListener;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.Conversation;
import com.example.mymessages.model.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    private RecyclerView recyclerViewContactsList;
    private ContactsAdapter adapter;
    private ArrayList<User> contactsList = new ArrayList<>();
    private DatabaseReference usersRef;
    private ValueEventListener valueEventListenerContacts;
    private FirebaseUser currentUser;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        //configuracoes iniciais
        recyclerViewContactsList = view.findViewById(R.id.recyclerViewContactsList);
        usersRef = ConfigFirebase.getFirebaseDatabase().child("users");
        currentUser = UsuarioFirebase.getUsuarioAtual(); //usuario atual para remover dos contatos

        //configurar o adapter
        adapter = new ContactsAdapter(contactsList, getActivity());

        //configurar o recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewContactsList.setLayoutManager(layoutManager);
        recyclerViewContactsList.setHasFixedSize(true);
        recyclerViewContactsList.setAdapter(adapter);

        //configurar evento de clique
        recyclerViewContactsList.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewContactsList,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                //abrir chat

                                //recupera lista de contatos (a de pesquisa ou normal)
                                List<User> updatedContactsList = adapter.getContacts();
                                User selectedUser = updatedContactsList.get(position);
                                //se email vazio, significa que é Novo grupo
                                boolean header = selectedUser.getEmail().isEmpty();

                                if(header){//se clicar em Novo grupo, abre activity de criar grupo
                                    Intent i = new Intent(getActivity(), GroupActivity.class);
                                    startActivity(i);

                                }else {//senao, abre chat
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContact", selectedUser);
                                    startActivity(i);
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                ));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        contactsList.clear();
        returnContacts();
    }

    @Override
    public void onStop() {
        super.onStop();
        usersRef.removeEventListener(valueEventListenerContacts);
    }

    //retorna contatos e adiciona na lista de contatos
    public void returnContacts(){

        //item clicar grupo
        /*em caso de email vazio, será utilizado como cabeçalho Novo grupo*/
        User itemGroup = new User();
        itemGroup.setName("Novo grupo");
        itemGroup.setEmail("");

        contactsList.add(itemGroup);

        //adicionar o restante dos contatos
        valueEventListenerContacts = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){

                    User user = data.getValue(User.class);
                    //só adicionar se o usuario nao for o usuario atual
                    if(!currentUser.getEmail().equals(user.getEmail())){
                        contactsList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void searchContacts(String text){
        /*este método percorre os contatos listadas e cria uma nova lista
        apenas com os contatos que contêm o texto pesquisado*/

        List<User> listContactsSearch = new ArrayList<>();

        for(User user : contactsList){

            String name = user.getName().toLowerCase();
            if(name.contains(text)){
                listContactsSearch.add(user);
            }
        }

        adapter = new ContactsAdapter(listContactsSearch, getActivity());
        recyclerViewContactsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //recarregar conversas
    public void updateContacts(){
        adapter = new ContactsAdapter(contactsList, getActivity());
        recyclerViewContactsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
