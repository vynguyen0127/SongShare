package com.example.songshare.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String playlistId;
    private String coverUrl;
    private String ownerUri;
    private String playlistName;
    public static final String TAG = "Playlist";
    Playlist(){}

    public Playlist(JSONObject jsonObject){
        try {
            JSONArray imagesArray = new JSONArray(jsonObject.get("images").toString());
            if(!imagesArray.isNull(0)) {
                JSONObject imageObject = new JSONObject(imagesArray.get(0).toString());
                coverUrl = imageObject.getString("url");
            }
            else{
                coverUrl = "";
            }

            playlistName = jsonObject.getString("name");
            playlistId = jsonObject.getString("id");

            JSONObject ownerObject = jsonObject.getJSONObject("owner");
            ownerUri = ownerObject.getString("uri");
            Log.i(TAG,"Cover: " + coverUrl + ", Playlist: " + playlistName + ", Id: " + playlistId + ", Owner: " + ownerUri);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static List<Playlist> fromJsonArray(JSONArray jsonArray){
        List<Playlist> playlists = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                playlists.add(new Playlist(jsonArray.getJSONObject(i)));

            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return playlists;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getOwnerUri() {
        return ownerUri;
    }

    public String getPlaylistName() {
        return playlistName;
    }


}
