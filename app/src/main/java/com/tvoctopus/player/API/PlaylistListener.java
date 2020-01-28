package com.tvoctopus.player.API;

import com.tvoctopus.player.model.Playlist;

public interface PlaylistListener {
    void playlistUpdated(Playlist playlist);
    void playlistWaited(boolean isWaiting);
}
