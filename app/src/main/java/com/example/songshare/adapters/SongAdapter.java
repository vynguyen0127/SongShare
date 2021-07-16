package com.example.songshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.songshare.PostDraftActivity;
import com.example.songshare.R;
import com.example.songshare.models.Song;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Song> songs;
    private Context context;
    private SpotifyAppRemote remote;

    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";

    public static final String TAG = "SongAdapter";

    public SongAdapter(Context context, List<Song> songs){
        this.context = context;
        this.songs = songs;

        connectRemote();
    }

    public void connectRemote(){
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        remote = spotifyAppRemote;
                        Log.d(TAG, "Connected! Yay!");

                        // Now you can start interacting with App Remote

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here

                        // Occurs when Spotify app not downloaded
                        if(throwable instanceof CouldNotFindSpotifyApp){
                            Toast.makeText(context,"WARNING: Spotify App not downloaded, please download the app and try again.",Toast.LENGTH_LONG).show();
                        }
                        // Occurs when User is not logged in to Spotify App
                        else if(throwable instanceof NotLoggedInException){
                            Toast.makeText(context,"WARNING: User is not logged into Spotify App.",Toast.LENGTH_LONG).show();
                        }
                        // Occurs when User does not give app permission to access Spotify Account
                        else if(throwable instanceof UserNotAuthorizedException){
                            Toast.makeText(context,"WARNING: Spotify User has not authorized permission.",Toast.LENGTH_LONG).show();
                        }
                        // Occurs when app cannot connect to Spotify app
                        else if(throwable instanceof SpotifyConnectionTerminatedException){
                            Toast.makeText(context,"WARNING: Connection to Spotify app has been terminated. Please restart the app and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void disconnectRemote(){
        Log.i(TAG,"disconnecting remote");
        SpotifyAppRemote.disconnect(remote);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_alt,parent,false);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivAlbum;
        TextView tvSongTitle;
        TextView tvArtist;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            ivAlbum = itemView.findViewById(R.id.ivCover);
            tvSongTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);

            itemView.setOnClickListener(this);
        }

        public void bind(Song song) {
            Glide.with(context)
                    .load(song.getAlbumUrl())
                    .transform(new RoundedCorners(20))
                    .into(ivAlbum);

            tvSongTitle.setText(song.getSongTitle());
            tvArtist.setText(song.getArtistName());

            ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remote.getPlayerApi().play(song.getSongId());
                    remote.getPlayerApi().seekToRelativePosition(35000);

                    Log.i(TAG, "Play clicked!");

                    Toast.makeText(context,"Now playing: " + song.getSongTitle() + " by "
                            + song.getArtistName(), Toast.LENGTH_LONG).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // change image
                            remote.getPlayerApi().pause();
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
                Intent i = new Intent(context, PostDraftActivity.class);
                i.putExtra("Song", Parcels.wrap(song));
                context.startActivity(i);
            }
        }
    }
}
