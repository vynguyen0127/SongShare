package com.example.songshare;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Song {

    private String albumUrl;
    private String artistName;
    private String songTitle;
    private String songId;
    Song(JSONObject jsonObject){
        try {
            JSONObject json4 = new JSONObject(jsonObject.getString("album"));
            JSONArray array2 = new JSONArray(json4.getString("images"));
            JSONObject json5 = new JSONObject(array2.get(1).toString());
            albumUrl = json5.getString("url").toString();

            JSONArray array3 = new JSONArray(jsonObject.getString("artists"));
            JSONObject json7 = new JSONObject(array3.get(0).toString());
            artistName = json7.getString("name").toString();

            songTitle = jsonObject.getString("name").toString();
            songId = jsonObject.getString("uri").toString();
            Log.i("Song", "Album: " + albumUrl + ", Artist: " + artistName + ", Song: " + songTitle + ", URI: " + songId);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static List<Song> fromJsonArray(JSONArray jsonArray){
        List<Song> songs = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                songs.add(new Song(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return songs;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongId() {
        return songId;
    }
}
