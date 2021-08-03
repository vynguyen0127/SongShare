package com.example.songshare.adapters;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private Context context;
    private List<Post> posts;
    private MainActivity.postMode mode;


    public PostsAdapter(Context context, List<Post> posts, MainActivity.postMode mode){
        this.context = context;
        this.posts = posts;
        this.mode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        layout = R.layout.item_post;

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


    public class ViewHolder extends RecyclerView.ViewHolder{

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
        Boolean visible = false;
        LinearLayout layoutCap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivAlbum = itemView.findViewById(R.id.ivCover);
            cvPost = itemView.findViewById(R.id.cvPost);
            linLayPost = itemView.findViewById(R.id.linLayPost);
            layoutCap = itemView.findViewById(R.id.layoutCap);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ibLike = itemView.findViewById(R.id.ibLike);


            if(mode == MainActivity.postMode.PROFILE){
                cvPost.setCardBackgroundColor(context.getResources().getColor(R.color.nadeshiko_pink));
                linLayPost.setBackgroundColor(context.getResources().getColor(R.color.orchid_pink));
            }


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(visible){
                        TransitionManager.beginDelayedTransition(cvPost,
                                new AutoTransition());
                        layoutCap.setVisibility(View.GONE);
                    }else{
                        TransitionManager.beginDelayedTransition(cvPost,
                                new AutoTransition());
                        layoutCap.setVisibility(View.VISIBLE);
                    }
                    visible = !visible;
                }
            });

        }

        public void bind(Post post) {

            Glide.with(context)
                    .load(post.getAlbumURL())
                    .transform(new RoundedCorners(20))
                    .into(ivAlbum);
            tvSongTitle.setText(post.getSongTitle());
            tvArtist.setText(post.getArtist());

            tvUsername.setText("@" + post.getUser().getUsername());
            tvCaption.setText(post.getCaption());
            tvCreatedAt.setText(post.calculateTimeAgo(post.getCreatedAt()));
            ibLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ufi_heart));
            liked = checkUserLiked(post);

            ibLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!liked){
                        likePost(post);
                    }
                }
            });


            ivAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((MainActivity)context).remote == null){
                        Toast.makeText(context, "Unable to connect to Spotify App",Toast.LENGTH_LONG).show();
                        return;
                    }
                    ((MainActivity)context).remote.getPlayerApi().play(post.getSongUri());

                    ((MainActivity)context).showPlayer();

                    Log.i(TAG, "Play clicked!");
                    Toast.makeText(context,"Now playing: " + post.getSongTitle() + " by " + post.getArtist(), Toast.LENGTH_LONG).show();

                }
            });


        }

        private void likePost(Post post) {
            {
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
                            Toast.makeText(context,"Liked post!",Toast.LENGTH_SHORT).show();
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


        private boolean checkUserLiked(Post post){
            Log.i(TAG,"Checking song liked.... " + post.getSongTitle());

            String userId = ParseUser.getCurrentUser().getObjectId();

            JSONArray a = post.getUsersLiked();
            for(int i = 0; i < a.length();i++){
                JSONObject j = null;
                try {
                    j = new JSONObject(a.get(i).toString());
                    if(Objects.equals(j.getString("objectId"), userId)){
                        Log.i(TAG,"user liked this post! Song: " + post.getSongTitle());
                        ibLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ufi_heart_active));
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return false;
        }

    }

}