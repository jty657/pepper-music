package com.example.peppermusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private ImageButton btnPlayPause, btnPrevious, btnNext, btnShuffle, btnRepeat;
    private ImageView albumArt;
    private TextView songTitle, artistName, timeText;
    private ProgressBar progressBar;

    private List<Song> songList = new ArrayList<>();
    private int currentSongIndex = -1;
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        initViews();

        // 请求存储权限
        requestStoragePermission();

        // 设置点击事件
        setupClickListeners();
    }

    private void initViews() {
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
        albumArt = findViewById(R.id.albumArt);
        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        timeText = findViewById(R.id.timeText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            loadSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                songTitle.setText("请授予存储权限以播放音乐");
            }
        }
    }

    private void loadSongs() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String albumArtPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                if (title != null && data != null) {
                    songList.add(new Song(title, artist, data, albumArtPath));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (songList.isEmpty()) {
            songTitle.setText(getString(R.string.no_songs));
        } else {
            playSong(0);
        }
    }

    private void playSong(int index) {
        if (index < 0 || index >= songList.size()) return;

        currentSongIndex = index;
        Song song = songList.get(index);

        songTitle.setText(song.title);
        artistName.setText(song.artist);
        progressBar.setProgress(0);
        timeText.setText("00:00 / 00:00");

        // 模拟专辑封面（实际应从媒体库加载）
        albumArt.setImageResource(R.drawable.ic_album_placeholder);

        // 模拟播放
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);

        // 启动后台服务播放音乐（此处为简化，实际需集成MediaPlayer或ExoPlayer）
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra("song_path", song.path);
        startService(serviceIntent);
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (currentSongIndex == -1) return;
            if (isPlaying) {
                pauseSong();
            } else {
                resumeSong();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1);
            } else if (isRepeat) {
                playSong(songList.size() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentSongIndex < songList.size() - 1) {
                playSong(currentSongIndex + 1);
            } else if (isRepeat) {
                playSong(0);
            }
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setImageResource(isShuffle ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_off);
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setImageResource(isRepeat ? R.drawable.ic_repeat_on : R.drawable.ic_repeat_off);
        });
    }

    private void pauseSong() {
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
        // 发送暂停指令给服务
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("PAUSE");
        startService(intent);
    }

    private void resumeSong() {
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        // 发送播放指令给服务
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("PLAY");
        startService(intent);
    }

    // 模拟歌曲类
    private static class Song {
        String title, artist, path, albumArtPath;

        Song(String title, String artist, String path, String albumArtPath) {
            this.title = title;
            this.artist = artist;
            this.path = path;
            this.albumArtPath = albumArtPath;
        }
    }
}