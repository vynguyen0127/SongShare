package com.example.songshare.models;

public class Artist {

    String name;
    String artistId;
    String imageUri;

    public Artist(String name, String artistUri, String imageUri){
        this.name = name;
        this.artistId = artistUri;
        this.imageUri = imageUri;
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
