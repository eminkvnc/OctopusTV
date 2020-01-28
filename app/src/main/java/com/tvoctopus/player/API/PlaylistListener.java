package com.tvoctopus.player.API;

public interface PlaylistListener {
    void playlistUpdated(Playlist playlist);
    void playlistWaited(boolean isWaiting);
}
