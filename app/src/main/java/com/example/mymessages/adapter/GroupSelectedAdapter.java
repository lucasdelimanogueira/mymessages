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

public class GroupSelectedAdapter extends RecyclerView.Adapter<GroupSelectedAdapter.MyViewHolder> {

    private List<User> selectedContacts;
    private Context context;

    public GroupSelectedAdapter(List<User> contactsList, Context c) {

        this.selectedContacts = contactsList;
        this.context = c;
    }

    @NonNull
    @Override
    public GroupSelectedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_group_selected, parent, false);
        return new GroupSelectedAdapter.MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSelectedAdapter.MyViewHolder holder, int position) {
        User user = selectedContacts.get(position);
        holder.name.setText(user.getName());

        //se o usuario tiver foto, colocar no circle image view, caso nao tenha coloca foto padrao
        if(user.getPhoto() != null){
            Uri uri = Uri.parse(user.getPhoto());
            Glide.with(context).load(uri).into(holder.photo);
        }else{
            holder.photo.setImageResource(R.drawable.padrao);
        }
    }

    @Override
    public int getItemCount() {
        return selectedContacts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView photo;
        TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.imageViewSelectedMemberPhoto);
            name = itemView.findViewById(R.id.textSelectedMemberName);
        }
    }
}
