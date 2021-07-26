package com.example.songshare.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.fragments.PlaylistAddFragment;
import com.example.songshare.fragments.PostDraftFragment;
import com.example.songshare.fragments.ResultFragment;
import com.example.songshare.fragments.SearchFragment;
import com.example.songshare.models.Song;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songs;
    private Context context;
    private SpotifyAppRemote remote;
    MainActivity.songMode songMode;
    Fragment fragment;

    String token;
    String uri;
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";

    public static final String TAG = "SongAdapter";

    public SongAdapter(Context context, List<Song> songs, MainActivity.songMode songMode, Fragment fragment){
        this.context = context;
        this.songs = songs;
        this.songMode = songMode;
        this.fragment = fragment;
    }

    public SongAdapter(Context context, List<Song> songs, MainActivity.songMode songMode){
        this.context = context;
        this.songs = songs;
        this.songMode = songMode;
    }

    public void setToken(String token){
        this.token = token;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        int layout;
        switch(songMode){
            case SEED:
                layout = R.layout.item_song;
                break;
            default:
                layout = R.layout.item_song_alt;
        }

        View view = LayoutInflater.from(context).
                inflate(layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        ImageView ivAlbum;
        TextView tvSongTitle;
        TextView tvArtist;
        CardView cvSong;
        LinearLayout linLaySong;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            ivAlbum = itemView.findViewById(R.id.ivCover);
            tvSongTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            cvSong = itemView.findViewById(R.id.cvSong);
            linLaySong = itemView.findViewById(R.id.linLaySong);


            switch(songMode){
                case SEED:
                    break;
                case RECOMMEND:
                case SEARCH:
                    cvSong.setCardBackgroundColor(context.getResources().getColor(R.color.nadeshiko_pink));
                    linLaySong.setBackgroundColor(context.getResources().getColor(R.color.orchid_pink));
                    itemView.setOnLongClickListener(this);
                    itemView.setOnClickListener(this);
                    break;
                default:
                    itemView.setOnClickListener(this);

            }

        }

        public void bind(Song song) {
            Glide.with(context)
                    .load(song.getAlbumUrl())
                    .transform(new RoundedCorners(30))
                    .into(ivAlbum);

            tvSongTitle.setText(song.getSongTitle());
            tvArtist.setText(song.getArtistName());


            ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)context).remote.getPlayerApi().play(song.getSongUri());
                    ((MainActivity)context).remote.getPlayerApi().seekToRelativePosition(35000);

                    Log.i(TAG, "Play clicked!");

                    Toast.makeText(context,"Now playing: " + song.getSongTitle() + " by "
                            + song.getArtistName(), Toast.LENGTH_LONG).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // change image
                            ((MainActivity)context).remote.getPlayerApi().pause();
                        }

                    }, 15000);

                }
            });


        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Log.i(TAG,"Song clicked!");
            if(position != RecyclerView.NO_POSITION){
                Song song = songs.get(position);
                if(fragment instanceof SearchFragment){
                    ((SearchFragment)fragment).goToFragment(song, new PostDraftFragment());
                }
                else if(fragment instanceof ResultFragment){
                    ((ResultFragment)fragment).goToFragment(song, new PostDraftFragment());
                }
            }


        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if(position != RecyclerView.NO_POSITION){
                Song song = songs.get(position);
                if(fragment instanceof SearchFragment){
                    ((SearchFragment)fragment).goToFragment(song,new PlaylistAddFragment());
                }
                else if(fragment instanceof ResultFragment){
                    ((ResultFragment)fragment).goToFragment(song, new PlaylistAddFragment());
                }

            }
            return false;
        }
    }
}
