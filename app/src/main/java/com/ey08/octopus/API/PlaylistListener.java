package com.ey08.octopus.API;

import com.ey08.octopus.model.Playlist;

public interface PlaylistListener {
    void playlistUpdated(Playlist playlist);
    void playlistWaited(boolean isWaiting);
}
