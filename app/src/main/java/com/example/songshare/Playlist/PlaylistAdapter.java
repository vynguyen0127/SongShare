package com.example.songshare.Playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.songshare.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    Context context;
    List<Playlist> playlists;

    public PlaylistAdapter(Context context, List<Playlist> playlists){
        this.context = context;
        this.playlists = playlists;
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

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlaylistName;
        ImageView ivCover;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            ivCover = itemView.findViewById(R.id.ivCover);
        }

        public void bind(Playlist playlist){
            tvPlaylistName.setText(playlist.getPlaylistName());
            Glide.with(context)
                    .load(playlist.getCoverUrl())
                    .transform(new RoundedCorners(20))
                    .into(ivCover);

        }
    }

}
