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
import com.example.mymessages.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {

    private List<User> contacts;
    private Context context;

    public ContactsAdapter(List<User> contactsList, Context c) {

        this.contacts = contactsList;
        this.context = c;
    }

    public List<User> getContacts(){
        return this.contacts;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contacts, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = contacts.get(position);
        boolean header = user.getEmail().isEmpty();

        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        //se o usuario tiver foto, colocar no circle image view, caso nao tenha coloca foto padrao
        if(user.getPhoto() != null){
            Uri uri = Uri.parse(user.getPhoto());
            Glide.with(context).load(uri).into(holder.photo);
        }else{
            //se email vazio, é Novo grupo, exibe icone de grupo
            if(header){
                holder.photo.setImageResource(R.drawable.icone_grupo);
                holder.email.setVisibility(View.GONE);
            }else {//se nao, exibe icone padrao
                holder.photo.setImageResource(R.drawable.padrao);
            }
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView photo;
        TextView name, email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.imageViewContactPhoto);
            name = itemView.findViewById(R.id.textContactName);
            email = itemView.findViewById(R.id.textContactEmail);
        }
    }

}
