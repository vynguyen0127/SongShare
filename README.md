Original App Design Project
===

# SongShare

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description


### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Music Sharing / Social Network
- **Mobile:** Easy to access anywhere. Microphone needed to detect music.
- **Story:** Allows creation of community among people with similar interests. Additionally acts as a place to discover new music
- **Market:** Avid music listeners of all ages.
- **Habit:** Users check daily to see what music others are listening to.
- **Scope:** V1 users can share songs to their feed and add ones they see to their own respective libraries. V2 music recognition feature to detect songs and post to feed. V3 listening rooms so that users can listen to playlists/albums with one another.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**
* Users can post a song to their feed with a caption
* Song posts are playable 
* Users can follow one another
* Users can add a song from their feed to their own playlist
* Users can like posts 
* Users can comment on posts
* Users can tap on post to get details
* Users can log in
* Users can sign up for an account

**Optional Nice-to-have Stories**

* Users can send posts/songs to one another
* Shazam-like feature to detect songs and post to feed
* Listening room so users can listen to songs at the same time?



### 2. Screen Archetypes

* Feed
   * Users can tap on post to get details
   * Users can add a song from their feed to their own playlist
* Post Details
   * Users can like posts
    * Users can comment on posts
* User Profile
    * Users can follow one another
* Search for songs
* Post Draft
    * Users can post a song to their feed with a caption
* Log in 
* Sign up

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home/Feed
* Compose
* Profile
* Settings
* Login Activity

**Flow Navigation** (Screen to Screen)

* Login
    * Sign-up
    * Home/Feed
* Home/Feed
   * Post Detail
   * List of Playlists (if user taps 'add' button)
   * Other user profile (if user taps on profile image?![]
(https://))
* Compose/Search
   * Post Draft
       * Home/Feed
* Profile

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="https://i.imgur.com/2Yl0Mbl.jpg" width=600>



### Networking

Home/Feed
- Calls to Spotify App Remote API to control Spotify App
- Calls to Parse Database to retrieve posts
- Calls to Spotify Web API to retrieve user's Spotify Playlists(GET https://api.spotify.com/v1/me/playlists)
- Calls to Spotify Web API to add to user's Spotify Playlist (POST https://api.spotify.com/v1/playlists/{playlist_id}/tracks)

Search
- Calls to Spotify App Remote API to control Spotify App
- Calls to Spotify Web API to search for tracks (GET https://api.spotify.com/v1/search)

Profile
- Calls to Spotify App Remote API to control Spotify App
- Calls to Spotify Web API to pull user's top tracks(GET https://api.spotify.com/v1/me/top/{type})

