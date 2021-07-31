package com.example.songshare.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Parcel
public class Song implements Comparable<Song> {

    private String albumUrl;
    private String artist;
    private String songTitle;
    private String songUri;
    private String songId;
    private String releaseDate;
    private int popularity;
    int year;
    int month;
    int day;
    Date date;
    Boolean explicit;

    public Song(){}

    public Song(String albumUrl, String artist, String songTitle, String songUri,String songId){
        this.albumUrl = albumUrl;
        this.artist = artist;
        this.songTitle = songTitle;
        this.songUri = songUri;
        this.songId = songId;
    }
    
    Song(JSONObject jsonObject){
        try {
            JSONObject jsonAlbum = new JSONObject(jsonObject.getString("album"));
            String release_date_precision = jsonAlbum.getString("release_date_precision");
            releaseDate = jsonAlbum.getString("release_date");
            if(Objects.equals(release_date_precision,"year")){
                releaseDate += "-01-01";
            }
            else if(Objects.equals(release_date_precision,"month")){
                releaseDate += "-01";
            }

            date = new Date(releaseDate);
            year = Integer.parseInt(releaseDate.substring(0, 4));
            month = Integer.parseInt(releaseDate.substring(5, 7));
            day = Integer.parseInt(releaseDate.substring(8, 10));

            JSONArray imagesArray = new JSONArray(jsonAlbum.getString("images"));
            JSONObject jsonImage = new JSONObject(imagesArray.get(0).toString());
            albumUrl = jsonImage.getString("url").toString();

            JSONArray artistArray = new JSONArray(jsonObject.getString("artists"));

            JSONObject jsonArtist = new JSONObject(artistArray.get(0).toString());
            artist = jsonArtist.getString("name").toString();

            songId = jsonObject.get("id").toString();
            songTitle = jsonObject.getString("name").toString();
            songUri = jsonObject.getString("uri").toString();
            String ex = jsonObject.getString("explicit");
            explicit = (Objects.equals(ex, "true"));

            popularity = Integer.parseInt(jsonObject.getString("popularity"));
            Log.i("Song", "Date: " + releaseDate + ", Popularity: " + popularity + ", Album: " + albumUrl + ", Artist: " + artist + ", Song: " + songTitle + ", URI: " + songUri);
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

    public String getArtist() {
        return artist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongUri() {
        return songUri;
    }

    public String getSongId() { return songId; }

    public int getPopularity(){ return popularity; }

    public String getReleaseDate() { return releaseDate; }

    public Date getDate(){ return date; }

    public void setDate(Date date){this.date = date;}

    public void setPopularity(int popularity){this.popularity = popularity; }

    public void print(Song song){
        Log.i("FILTER",
                "Date: " + song.getReleaseDate() +
                        ", Popularity: " + song.getPopularity() +
                        ", Album: " + song.getAlbumUrl() +
                        ", Artist: " + song.getArtist() +
                        ", Song: " + song.getSongTitle() +
                        ", URI: " + song.getSongUri());
    }

    @Override
    public int compareTo(Song o) {
        return 0;
    }
}




