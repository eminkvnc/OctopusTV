package com.ey08.octopus.API;

public interface DownloadCompleteListener {
    void downloadComplete(boolean isAllDownloadsComplete);
    void downloadStart();
}
