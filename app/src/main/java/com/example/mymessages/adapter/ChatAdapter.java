package com.example.mymessages.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymessages.R;
import com.example.mymessages.model.Conversation;
import com.example.mymessages.model.Group;
import com.example.mymessages.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Conversation> conversations;
    private Context context;

    public ChatAdapter(List<Conversation> list, Context c) {
        this.conversations = list;
        this.context = c;
    }

    public List<Conversation> getConversations(){
        return this.conversations;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contacts, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        //recupera a conversa pela posição
        Conversation conversation = conversations.get(position);

        //ultima mensagem na conversa
        holder.lastMessage.setText(conversation.getLastMessage());

        //verifica se é um grupo ou conversa entre dois usuarios
        if(conversation.getIsGroup().equals("true")){//grupo

            Group group = conversation.getGroup();
            holder.name.setText(group.getName());

            //verificar se existe foto
            if(group.getPhoto() != null){
                Uri uri = Uri.parse(group.getPhoto());
                Glide.with(context).load(uri).into(holder.photo);
            }else{
                holder.photo.setImageResource(R.drawable.padrao);
            }

        }else{//conversa entre dois usuarios

            //nome do usuario na conversa
            User user = conversation.getDisplayedUser();
            if(user != null){

                holder.name.setText(user.getName());

                //verificar se existe foto
                if(user.getPhoto() != null){
                    Uri uri = Uri.parse(user.getPhoto());
                    Glide.with(context).load(uri).into(holder.photo);
                }else{
                    holder.photo.setImageResource(R.drawable.padrao);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView photo;
        TextView name, lastMessage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.imageViewContactPhoto);
            name = itemView.findViewById(R.id.textContactName);
            lastMessage = itemView.findViewById(R.id.textContactEmail);
        }
    }
}
