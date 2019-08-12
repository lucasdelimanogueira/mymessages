package com.example.mymessages.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymessages.R;
import com.example.mymessages.helper.UsuarioFirebase;
import com.example.mymessages.model.Message;

import org.w3c.dom.Text;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MyViewHolder> {

    private List<Message> messages;
    private Context context;
    private static final int SENDER_TYPE = 0;
    private static final int RECIPIENT_TYPE = 1;

    public MessagesAdapter(List<Message> list, Context c) {
        this.messages = list;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;

        //se a mensagem foi de um rementente
        if(viewType == SENDER_TYPE){
            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_message_sender, parent, false);

        }else if(viewType == RECIPIENT_TYPE){//se a mensagem foi de um destinatário
            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_message_recipient, parent, false);

        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messages.get(position);
        String msg = message.getMessage();
        String image = message.getImage();

        //só exibir mensagem ou imagem
        if(image != null){
            Uri url = Uri.parse(image);
            Glide.with(context).load(url).into(holder.image);

            String userName = message.getName();
            if(!userName.isEmpty()){//se nome nao é vazio, é uma mensagem de grupo
                holder.name.setText(userName);
            }else{
                holder.name.setVisibility(View.GONE);
            }

            //esconder o texto
            holder.message.setVisibility(View.GONE);
        }else{
            holder.message.setText(msg);

            String userName = message.getName();
            if(!userName.isEmpty()){//se nome nao é vazio, é uma mensagem de grupo
                holder.name.setText(userName);
            }else{
                holder.name.setVisibility(View.GONE);
            }

            //esconder a imagem
            holder.image.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        Message message = messages.get(position);

        String userId = UsuarioFirebase.getUserId();
        //se id do usuario atual (rementente) é igual ao id do usuario da mensagem
        if(userId.equals(message.getUserId())){
            return SENDER_TYPE;
        }
        return RECIPIENT_TYPE;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView message;
        TextView name;
        ImageView image;

        public  MyViewHolder(View itemView){
            super(itemView);

            message = itemView.findViewById(R.id.textMessageText);
            name = itemView.findViewById(R.id.textMessageUserName);
            image = itemView.findViewById(R.id.imageMessageImage);
        }
    }
}
