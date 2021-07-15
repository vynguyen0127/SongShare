package com.example.songshare.Post;

import android.content.Context;
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
import com.example.songshare.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    Context context;
    List<Post> posts;
    SpotifyAppRemote remote;
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";
    public PostsAdapter(Context context, List<Post> posts){
        this.context = context;
        this.posts = posts;

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
//                        Toast.makeText(context,"Connected! Yay!",Toast.LENGTH_SHORT).show();
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

    public void disconnectRemote(){
        Log.i(TAG,"disconnecting remote");
        SpotifyAppRemote.disconnect(remote);
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvSongTitle;
        TextView tvUsername;
        TextView tvArtist;
        ImageView ivAlbum;
        TextView tvCaption;
        TextView tvCreatedAt;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvPlaylistName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivAlbum = itemView.findViewById(R.id.ivCover);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);

            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {

            Glide.with(context)
                    .load(post.getAlbumURL())
                    .transform(new RoundedCorners(20))
                    .into(ivAlbum);

            tvUsername.setText(post.getUser().getUsername());
            tvCaption.setText(post.getCaption());
            tvSongTitle.setText(post.getSongTitle());
            tvArtist.setText(post.getArtist());
            tvCreatedAt.setText(post.calculateTimeAgo(post.getCreatedAt()));


            ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(remote == null){
                        Toast.makeText(context, "Unable to connect to Spotify App",Toast.LENGTH_LONG).show();
                        return;
                    }
                    remote.getPlayerApi().play(post.getSongID());

                    Handler handler = new Handler();

                    Log.i(TAG, "Play clicked!");
                    Toast.makeText(context,"Now playing: " + post.getSongTitle(), Toast.LENGTH_LONG).show();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // change image
                            remote.getPlayerApi().pause();
                        }

                    }, 10000);

                }
            });

        }


        @Override
        public void onClick(View v) {

        }
    }
}