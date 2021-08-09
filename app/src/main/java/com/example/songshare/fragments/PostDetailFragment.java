package com.example.songshare.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.models.Post;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostDetailFragment extends Fragment {

    private Post post;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private ImageView ivAlbum;

    TextView tvPopularity;
    TextView tvReleaseDate;
    TextView tvCaption;

    String pop;
    String release;
    String accessToken;
    OkHttpClient okHttpClient = new OkHttpClient();
    public static final String TAG = "PostDetailFragment";


    public PostDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();

        post = bundle.getParcelable("post");

        tvSongTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        ivAlbum = view.findViewById(R.id.ivCover);
        tvPopularity = view.findViewById(R.id.tvPopularity);
        tvReleaseDate = view.findViewById(R.id.tvReleaseDate);

        tvSongTitle.setText(post.getSongTitle());
        tvArtist.setText(post.getArtist());
        Glide.with(getContext())
                .load(post.getAlbumURL())
                .into(ivAlbum);
        makeRequest();

        ivAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getContext()).remote.getPlayerApi().play(post.getSongUri());
                ((MainActivity)getContext()).showPlayer();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_post_detail, menu);
        final MenuItem deleteItem = menu.findItem(R.id.delete);

        final MenuItem captionItem = menu.findItem(R.id.caption);
        if(!Objects.equals(post.getUser().getUsername(),ParseUser.getCurrentUser().getUsername())){
            captionItem.setVisible(false);
            deleteItem.setVisible(false);
        }
        else{
            deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    deletePost();
                    return true;
                }
            });

            captionItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return false;
                }
            });

        }

    }


    private void deletePost(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        String postObjectId = post.getObjectId();
        // Retrieve the object by id
        query.getInBackground(postObjectId, (object, e) -> {
            if (e == null) {
                //Object was fetched
                //Deletes the fetched ParseObject from the database
                object.deleteInBackground(e2 -> {
                    if(e2==null){
                        Log.i(TAG,"Delete successful");
                        navigateHome();
                    }else{
                        //Something went wrong while deleting the Object
                        Log.i(TAG,"Error: "+e2.getMessage());
                    }
                });
            }else{
                //Something went wrong
                Log.i(TAG,e.getMessage());
            }
        });

    }

    private void navigateHome(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();

        Fragment feedFragment = new FeedFragment();
        String backStateName = feedFragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, feedFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        fragmentManager.beginTransaction().replace(R.id.flContainer,feedFragment).commit();
        MainActivity.bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private String extractId(){
        String temp;
        temp = post.getSongUri().toString();
        temp = temp.substring(14);
        Log.i(TAG,"extracted id: " + temp);
        return temp;
    }

    private void makeRequest () {

        accessToken = ((MainActivity)this.getActivity()).getAccessTokenSpotify();

        if (accessToken == null) {
            Log.e(TAG,"no token");
            return;
        }

        String url = getUrl();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        call(request);

    }

    private String getUrl(){
        String temp = extractId();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format("https://api.spotify.com/v1/tracks/%s",temp)).newBuilder();
        return urlBuilder.build().toString();
    }

    private void call(Request request){
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG,"onFailure");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {

                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);

                    pop = json.getString("popularity");

                    JSONObject album = json.getJSONObject("album");
                    release = album.getString("release_date");
                    tvPopularity.setText(pop);
                    tvReleaseDate.setText(release);
                    Log.i(TAG,"json: " + album.toString());

                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }
            }

        });
    }
}
