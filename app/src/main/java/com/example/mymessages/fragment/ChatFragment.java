package com.example.mymessages.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.mymessages.R;
import com.example.mymessages.activity.ChatActivity;
import com.example.mymessages.adapter.ChatAdapter;
import com.example.mymessages.config.ConfigFirebase;
import com.example.mymessages.helper.RecyclerItemClickListener;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.Conversation;
import com.example.mymessages.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewChat;
    private List<Conversation> listConversation = new ArrayList<>();
    private ChatAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversationRef;
    private ChildEventListener childEventListenerConversations;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerViewChat = view.findViewById(R.id.recyclerViewChatList);

        //configurar adapter
        adapter = new ChatAdapter(listConversation, getActivity());

        //configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setAdapter(adapter);

        //configura evento de clique na conversa
        recyclerViewChat .addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewChat,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                //abrir conversa

                                //recupera lista de conversa (a de pesquisa ou normal)
                                List<Conversation> updatedConversationList = adapter.getConversations();
                                Conversation selectedConversation = updatedConversationList.get(position);

                                //verifica se é um grupo
                                if(selectedConversation.getIsGroup().equals("true")){//é um grupo

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGroup", selectedConversation.getGroup());
                                    startActivity(i);

                                }else{//conversa entre duas pessoas

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContact", selectedConversation.getDisplayedUser());
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
                )
        );

        //configura conversas ref
        String userIdentification = UsuarioFirebase.getUserId();
        database = ConfigFirebase.getFirebaseDatabase();
        conversationRef = database.child("conversations").child(userIdentification);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listConversation.clear();
        returnConversations();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversationRef.removeEventListener(childEventListenerConversations);
    }

    public void searchConversations(String text){
        /*este método percorre as conversas listadas e cria uma nova lista
        apenas com as conversas que contêm o texto pesquisado no nome ou ultima mensagem*/

        List<Conversation> listConversationSearch = new ArrayList<>();

        for(Conversation conversation : listConversation){

            if(conversation.getDisplayedUser() != null){//conversa normal

                String name = conversation.getDisplayedUser().getName().toLowerCase();
                String lastMessage = conversation.getLastMessage().toLowerCase();

                if(name.contains(text) || lastMessage.contains(text)){
                    listConversationSearch.add(conversation);
                }

            }else{//conversa de grupo

                String name = conversation.getGroup().getName().toLowerCase();
                String lastMessage = conversation.getLastMessage().toLowerCase();

                if(name.contains(text) || lastMessage.contains(text)){
                    listConversationSearch.add(conversation);
                }
            }
        }

        adapter = new ChatAdapter(listConversationSearch, getActivity());
        recyclerViewChat.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //recarregar conversas
    public void updateConversations(){
        adapter = new ChatAdapter(listConversation, getActivity());
        recyclerViewChat.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void returnConversations(){

        childEventListenerConversations = conversationRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //recuperar conversas
                Conversation conversation = dataSnapshot.getValue(Conversation.class);
                listConversation.add(conversation);
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
}
