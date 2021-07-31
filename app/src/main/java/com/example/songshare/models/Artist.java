package com.example.songshare.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Artist {

    String name;
    String artistId;
    String imageUri;

    public Artist(String name, String artistId, String imageUri){
        this.name = name;
        this.artistId = artistId;
        this.imageUri = imageUri;
    }

    public Artist(JSONObject object){
        try {
            name = object.getString("name");
            artistId = object.getString("id");

            JSONArray images = new JSONArray(object.get("images").toString());
            JSONObject image = new JSONObject(images.get(1).toString());
            imageUri = image.getString("url");

        }catch(JSONException e){
            imageUri = "";
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getImageUri(){
        return imageUri;
    }
}
