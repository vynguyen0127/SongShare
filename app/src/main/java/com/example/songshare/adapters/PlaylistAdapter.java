package com.example.songshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.songshare.PlaylistAddActivity;
import com.example.songshare.R;
import com.example.songshare.fragments.PlaylistAddFragment;
import com.example.songshare.models.Playlist;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    Context context;
    List<Playlist> playlists;
    Fragment fragment;

    public PlaylistAdapter(Context context, List<Playlist> playlists){
        this.context = context;
        this.playlists = playlists;
    }

    public PlaylistAdapter(Context context, List<Playlist> playlists, Fragment fragment){
        this.context = context;
        this.playlists = playlists;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist);
    }


    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void clear(){
        playlists.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvPlaylistName;
        ImageView ivCover;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvPlaylistName = itemView.findViewById(R.id.tvTitle);
            ivCover = itemView.findViewById(R.id.ivCover);

            itemView.setOnClickListener(this);
        }

        public void bind(Playlist playlist){
            tvPlaylistName.setText(playlist.getPlaylistName());
            if(!playlist.getCoverUrl().isEmpty()) {
                Glide.with(context)
                        .load(playlist.getCoverUrl())
                        .transform(new RoundedCorners(20))
                        .into(ivCover);
            }
            else{
                Glide.with(context)
                        .load(R.drawable.ic_baseline_audiotrack_24)
                        .transform(new RoundedCorners(20))
                        .into(ivCover);
            }
        }

        @Override
        public void onClick(View v) {
            Playlist item = playlists.get(getAdapterPosition());
            if(fragment == null)
                ((PlaylistAddActivity)context).addSong(item.getPlaylistId());
            else
                ((PlaylistAddFragment)fragment).addSong(item.getPlaylistId());


        }
    }

}
