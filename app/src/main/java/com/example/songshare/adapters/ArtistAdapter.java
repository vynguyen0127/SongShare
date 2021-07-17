package com.example.songshare.adapters;

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
import com.bumptech.glide.request.RequestOptions;
import com.example.songshare.R;
import com.example.songshare.models.Artist;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    List<Artist> artists;
    Context context;
    public ArtistAdapter(Context context, List<Artist> artists){
        this.context = context;
        this.artists = artists;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_artist,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ArtistAdapter.ViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.bind(artist);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivArtistImage;
        TextView tvArtistName;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            ivArtistImage = itemView.findViewById(R.id.ivArtistImage);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
        }

        public void bind(Artist artist){
            Glide.with(context)
                    .load(artist.getImageUri())
                    .transform(new RoundedCorners(20))
                    .apply(new RequestOptions().override(300, 300))
                    .into(ivArtistImage);

            tvArtistName.setText(artist.getName());
        }
    }
}
