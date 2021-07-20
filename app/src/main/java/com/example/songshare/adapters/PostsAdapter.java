package com.example.songshare.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.models.Post;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;
    private SpotifyAppRemote remote;
    private MainActivity.postMode mode;
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";
    public PostsAdapter(Context context, List<Post> posts, MainActivity.postMode mode){
        this.context = context;
        this.posts = posts;
        this.mode = mode;
        connectRemote();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        if(mode == MainActivity.postMode.FEED){
            layout = R.layout.item_post;
        }else{
            layout = R.layout.item_post_alt;
        }

        View view = LayoutInflater.from(context).inflate(layout,parent,false);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvSongTitle;
        TextView tvUsername;
        TextView tvArtist;
        ImageView ivAlbum;
        TextView tvCaption;
        TextView tvCreatedAt;
        ImageButton ibLike;
        CardView cvPost;
        LinearLayout linLayPost;
        Boolean liked = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivAlbum = itemView.findViewById(R.id.ivCover);
            cvPost = itemView.findViewById(R.id.cvPost);
            linLayPost = itemView.findViewById(R.id.linLayPost);
            if(mode == MainActivity.postMode.FEED) {
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvCaption = itemView.findViewById(R.id.tvCaption);
                tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
                ibLike = itemView.findViewById(R.id.ibLike);
            }
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {

            Glide.with(context)
                    .load(post.getAlbumURL())
                    .transform(new RoundedCorners(20))
                    .into(ivAlbum);
            tvSongTitle.setText(post.getSongTitle());
            tvArtist.setText(post.getArtist());

            if(mode == MainActivity.postMode.FEED) {
                tvUsername.setText("@" + post.getUser().getUsername());
                tvCaption.setText(post.getCaption());
                tvCreatedAt.setText(post.calculateTimeAgo(post.getCreatedAt()));
                ibLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ufi_heart));
                checkUserLiked(post);

                ibLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!liked){

                            // add post to user's liked_posts array
                            int position = getAdapterPosition();
                            Post temp = posts.get(position);

                            ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                            String objectId = ParseUser.getCurrentUser().getObjectId();
                            // The query will search for a ParseObject, given its objectId.
                            // When the query finishes running, it will invoke the GetCallback
                            // with either the object, or the exception thrown
                            query.getInBackground(objectId, (object, e) -> {
                                if (e == null) {
                                    //Object was successfully retrieved
                                    Log.i(TAG, "object retrieved");
                                    object.add("liked_posts", temp);
                                    try {
                                        Log.i(TAG,"set liked "+ post.getSongTitle());
                                        ibLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ufi_heart_active));
                                        object.save();
                                    } catch (ParseException parseException) {
                                        parseException.printStackTrace();
                                    }
                                } else {
                                    // something went wrong
                                    Log.i(TAG, e.getMessage());
                                }
                            });

                            query = ParseQuery.getQuery("Post");
                            objectId = post.getObjectId();

                            query.getInBackground(objectId, (object, e) -> {
                                if (e == null) {
                                    object.add("users_liked", ParseUser.getCurrentUser());
                                    try {
                                        object.save();
                                    } catch (ParseException parseException) {
                                        parseException.printStackTrace();
                                    }
                                } else {
                                    Log.i(TAG, e.getMessage());
                                }
                            });
                        }
                    }
                });
            }

            ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(remote == null){
                        Toast.makeText(context, "Unable to connect to Spotify App",Toast.LENGTH_LONG).show();
                        return;
                    }
                    remote.getPlayerApi().play(post.getSongUri());

                    Log.i(TAG, "Play clicked!");
                    Toast.makeText(context,"Now playing: " + post.getSongTitle(), Toast.LENGTH_LONG).show();

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

        }

        private void checkUserLiked(Post post){
            Log.i(TAG,"Checking song liked.... " + post.getSongTitle());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
            String objectId = post.getObjectId();
            String userId = ParseUser.getCurrentUser().getObjectId();
            query.getInBackground(objectId,(object, e)->{
                if(e == null){
                    final JSONArray array = object.getJSONArray("users_liked");
                    for(int i = 0; i < array.length(); i++){
                        try {
                            JSONObject j = new JSONObject(array.get(i).toString());
                            Log.i(TAG,"object: " + j.getString("objectId"));
                            if(Objects.equals(j.getString("objectId"), userId)){
                                Log.i(TAG,"user liked this post! Song: " + post.getSongTitle());
                                ibLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ufi_heart_active));
                                liked = true;
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                    }

                }else{
                    Log.i(TAG,e.getMessage());
                }
            });
            liked = false;
        }
    }
}