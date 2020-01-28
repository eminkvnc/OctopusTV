package com.tvoctopus.player.API;

public interface DownloadCompleteListener {
    void downloadComplete(boolean isAllDownloadsComplete);
    void downloadStart();
}
