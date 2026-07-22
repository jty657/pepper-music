package com.example.peppermusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

import java.io.IOException;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private String currentSongPath = "";
    private boolean isPlaying = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String songPath = intent.getStringExtra("song_path");

            if (action != null) {
                if (action.equals("PLAY")) {
                    if (!isPlaying && currentSongPath != null && !currentSongPath.isEmpty()) {
                        playSong();
                    }
                } else if (action.equals("PAUSE")) {
                    pauseSong();
                }
            } else if (songPath != null && !songPath.equals(currentSongPath)) {
                // 新歌播放
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                currentSongPath = songPath;
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(currentSongPath);
                    mediaPlayer.prepare();
                    playSong();
                } catch (IOException e) {
                    Log.e("MusicService", "Error loading song: " + e.getMessage());
                }
            }
        }
        return START_STICKY;
    }

    private void playSong() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}