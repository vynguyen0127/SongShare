package com.example.songshare;

import com.example.songshare.models.Song;

import java.util.Comparator;

public class SongComparator implements Comparator<Song> {

    MainActivity.sortMode mode;
    DateComparator comparator;

    public SongComparator(MainActivity.sortMode mode){
        this.mode = mode;
        comparator = new DateComparator();
    }

    @Override
    public int compare(Song o1, Song o2) {
        switch(mode){
            case POPULARITY:
                return Integer.compare(o1.getPopularity(),o2.getPopularity());
            case POPULARITY_REVERSE:
                return Integer.compare(o2.getPopularity(),o1.getPopularity());
            case RELEASE:
                 return comparator.compare(o1.getDate(),o2.getDate());
            case RELEASE_REVERSE:
                return comparator.compare(o2.getDate(),o1.getDate());
            default:
                return 0;
        }

    }

}

