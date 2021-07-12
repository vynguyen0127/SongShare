package com.example.songshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostAdapter";

    Context context;
    List<Post> posts;
    SpotifyAppRemote remote;
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";
    public PostsAdapter(Context context, List<Post> posts){
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

        TextView tvSongTitle;
        TextView tvUsername;
        TextView tvArtist;
        ImageButton ibPlay;
        ImageView ivAlbum;
        TextView tvCaption;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ibPlay = itemView.findViewById(R.id.ibPlay);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);
            tvCaption = itemView.findViewById(R.id.tvCaption);

            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            Glide.with(context)
                    .load(post.getAlbumURL())
                    .into(ivAlbum);

            tvUsername.setText(post.getUser().getUsername());
            tvCaption.setText(post.getCaption());
//            tvSongTitle.setText(post.getSongTitle());
//            tvArtist.setText(post.getArtist());

            // Set the connection parameters
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
                            Log.d("FeedFragment", "Connected! Yay!");

                            // Now you can start interacting with App Remote
//                        connected();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e("FeedFragment", throwable.getMessage(), throwable);

                            // Something went wrong when attempting to connect! Handle errors here
                        }
                    });
            ibPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remote.getPlayerApi().play(post.getSongID());
                    remote.getPlayerApi()
                            .subscribeToPlayerState()
                            .setEventCallback(playerState -> {
                                final Track track = playerState.track;
                                if (track != null) {
                                    Log.d("PostsAdapter", track.name + " by " + track.artist.name);
                                }
                            });

                    Log.i("PostsAdapter", "Play clicked!");
                    Toast.makeText(context,"Now playing: " + post.getSongTitle(), Toast.LENGTH_LONG).show();

                }
            });

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