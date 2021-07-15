package com.example.songshare.Playlist;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private String playlistId;
    private String coverUrl;
    private String ownerId;
    private String playlistName;

    Playlist(){}

    Playlist(JSONObject jsonObject){}

    public static List<Playlist> fromJsonArray(JSONArray jsonArray){
        List<Playlist> playlists = new ArrayList<>();

        return playlists;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getPlaylistName() {
        return playlistName;
    }
}
