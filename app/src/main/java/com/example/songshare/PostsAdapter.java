package com.example.songshare;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostAdapter";

    Activity context;
    List<Post> posts;

    PostsAdapter(Activity context, List<Post> posts){
        this.context = context;
        this.posts = posts;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvsongID;
        TextView tvUsername;
        ImageButton ibPlay;
        ImageView ivAlbum;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvsongID = itemView.findViewById(R.id.tvSongID);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ibPlay = itemView.findViewById(R.id.ibPlay);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);

            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            tvsongID.setText(post.getSongID());
            tvUsername.setText(post.getUser().getUsername());
        }

        @Override
        public void onClick(View view){
            Log.i(TAG,"post clicked!");
            int position =  getAdapterPosition();

            if(position != RecyclerView.NO_POSITION){

            }
        }
    }
}