package com.ey08.octopus.API;

public interface PlaylistListener {
    void playlistUpdated(Playlist playlist);
    void playlistWaited(boolean isWaiting);
}
