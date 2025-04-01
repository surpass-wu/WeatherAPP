package com.example.weather.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.R;
import com.example.weather.ui.lrc.ILrcBuilder;
import com.example.weather.ui.lrc.ILrcView;
import com.example.weather.ui.lrc.ILrcViewListener;
import com.example.weather.ui.lrc.impl.DefaultLrcBuilder;
import com.example.weather.ui.lrc.impl.LrcRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MP3View extends AppCompatActivity {

    private static MediaPlayer mediaPlayer;
    private Button playButton, forwardButton, backwardButton , nextsongButton , presongButton , soundButton;
    private Button backButton , addBtn , listBtn;
    private SeekBar seekBar;
    private boolean isUserSeeking = false;
    private static boolean pauseplay;
    private static int idx;
    private TextView songTitleTextView;
    //自定义LrcView，用来展示歌词
    private static ILrcView mLrcView;
    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 1000;
    //更新歌词的定时器
    private static Timer mTimer;
    //更新歌词的定时任务
    private static TimerTask mTask;
    private RelativeLayout layout;
    private int backgroundcolor;
    private SeekBar seekVol;
    private static int alpha;
    public static List<String> song_urls;
    public static List<String> song_words_urls;
    private static AudioManager audioManger;
    private static boolean isMuted;
    private static int previousVolume;
    private static String lrc;
    private static View dialogLayout;

    private BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int currentVolume = audioManger.getStreamVolume(AudioManager.STREAM_MUSIC);
                seekVol.setProgress(currentVolume);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        hideSystemUI();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);

        if(song_urls == null)
        {
            song_urls = new ArrayList<>();
            song_words_urls = new ArrayList<>();
            scanAssets(getApplicationContext());
        }

        layout = findViewById(R.id.totalayout);
        soundButton = findViewById(R.id.song_sound);
        mLrcView=(ILrcView)findViewById(R.id.lrcView);
        playButton = findViewById(R.id.play_button);
        forwardButton = findViewById(R.id.forward_button);
        backwardButton = findViewById(R.id.backward_button);
        nextsongButton = findViewById(R.id.next_song);
        presongButton = findViewById(R.id.previous_song);
        backButton = findViewById(R.id.back_button);
        seekBar = findViewById(R.id.seek_bar);
        addBtn = findViewById(R.id.add_song);
        listBtn = findViewById(R.id.list);
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSongListDialog();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("add" , "ss");
                showImportDialog();
            }
        });

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            idx = 0;
            playCurrentSong();
            pauseplay = false;
            isMuted = false;
        }
        if(audioManger == null){
            audioManger = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        int maxVol = audioManger.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVol = audioManger.getStreamVolume(AudioManager.STREAM_MUSIC);

        registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        seekVol = findViewById(R.id.seekVol);
        seekVol.setMax(maxVol);
        seekVol.setProgress(curVol);

        backgroundcolor = getRandomColor();
        // 获取初始音量
        int initialVolume = audioManger.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 计算初始背景颜色透明度
        int initialColor = calculateColor(initialVolume , backgroundcolor);
        // 将初始背景颜色应用于布局
        GradientDrawable initialDrawable = (GradientDrawable) layout.getBackground();
        initialDrawable.setColors(new int[] {Color.WHITE, initialColor});

        if(initialVolume > 0){
            soundButton.setBackgroundResource(R.drawable.sound);
        }
        if(initialVolume == 0){
            soundButton.setBackgroundResource(R.drawable.sound_shut);
        }

        seekVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManger.setStreamVolume(AudioManager.STREAM_MUSIC, progress,0);
                if(audioManger.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
                    soundButton.setBackgroundResource(R.drawable.sound);
                }
                if(audioManger.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
                    soundButton.setBackgroundResource(R.drawable.sound_shut);
                }
                int color = calculateColor(progress , backgroundcolor);
                GradientDrawable drawable = (GradientDrawable) layout.getBackground();
                drawable.setColors(new int[] {Color.WHITE, color});
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mLrcView.setListener(new ILrcViewListener() {
            //当歌词被用户上下拖动的时候回调该方法,从高亮的那一句歌词开始播放
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (mediaPlayer != null) {
                    Log.d("TAG", "onLrcSeeked:" + row.time);
                    mediaPlayer.seekTo((int) row.time);
                }
            }
        });

        if(mediaPlayer.isPlaying())
        {
            playButton.setBackgroundResource(R.drawable.pause);
        }
        if(!mediaPlayer.isPlaying())
        {
            playButton.setBackgroundResource(R.drawable.play);
        }

        nextsongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playNextSong();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        presongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idx = (idx - 1 + song_urls.size()) % song_urls.size();
                playCurrentSong();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pauseplay){
                    mediaPlayer.start();
                    pauseplay = false;
                    playButton.setBackgroundResource(R.drawable.pause);
                }
                else{
                    mediaPlayer.pause();
                    pauseplay = true;
                    playButton.setBackgroundResource(R.drawable.play);
                }
            }
        });

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMute();
            }
        });


        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
            }
        });

        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
            }
        });

        seekBar.setMax(mediaPlayer.getDuration());

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && !isUserSeeking) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                }
                // 每隔一段时间更新一次进度条
                handler.postDelayed(this, 500); // 500ms 即每秒钟更新一次
            }
        }, 0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
            }
        });


    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("导入音频文件");
        builder.setMessage("是否从本地导入音频文件？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 检查权限
                if (checkPermission()) {
                    // 已授权，开始导入音频文件
                    importAudioFiles();
                } else {
                    // 未授权，请求权限
                    requestPermission();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private static final int PERMISSION_REQUEST_CODE = 100;

    // 检查权限
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    // 请求权限
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授权了权限，开始导入音频文件
                importAudioFiles();
            } else {
                // 用户拒绝了权限，给出提示
                Toast.makeText(this, "请授予访问本地音频文件的权限", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                // 启动设置页面
                startActivity(intent);
            }
        }
    }

    private void importAudioFiles() {
        int importedCount = 0; // 记录成功导入的音频文件数量
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                // 检查文件是否已存在，如果存在则跳过
                if (song_urls.contains(filePath)) {
                    continue;
                }

                // 更新歌曲列表
                song_urls.add(filePath);

                // 导入对应的歌词文件（假设歌词文件与音频文件同名，后缀为.lrc）
                String lrcFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".lrc";
                // 如果歌词文件存在，也更新歌词列表
                song_words_urls.add(lrcFileName);

                // 更新成功导入的音频文件数量
                importedCount++;
            }
            cursor.close();
        }

        // 导入结束后显示对话框，提示成功导入的音频文件数量
        showImportSuccessDialog(importedCount);
    }


    private void showImportSuccessDialog(int importedCount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("导入音频文件成功");
        builder.setMessage("成功添加了 " + importedCount + " 个音频文件！");
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (songTitleTextView == null) {
            songTitleTextView = findViewById(R.id.song_title_text_view);
        }
        if(mLrcView == null)
        {
            mLrcView = findViewById(R.id.lrcView);
        }
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);
        songTitleTextView.setText(song_urls.get(idx));
    }


    private void playNextSong() {
        idx = (idx + 1) % song_urls.size();
        playCurrentSong();
    }

    private void playCurrentSong() {
        synchronized (mediaPlayer) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }

        // 设置新的歌曲
        try {
            lrc = getFromAssets(song_words_urls.get(idx));
            ILrcBuilder builder = new DefaultLrcBuilder();
            List<LrcRow> rows = builder.getLrcRows(lrc);
            mLrcView.setLrc(rows);
            String filePath = song_urls.get(idx);

            try {
                if (filePath.startsWith("asset://")) {
                    // 来自Assets文件夹的文件
                    String assetPath = filePath.substring("asset://".length());
                    AssetFileDescriptor descriptor = getAssets().openFd(assetPath);
                    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();
                } else {
                    // 其他来源的文件（例如本地文件）
                    mediaPlayer.setDataSource(filePath);
                }
                // 设置数据源成功，开始准备播放
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
                // 发生异常，无法播放该歌曲，跳转到下一首歌曲
                playNextSong();
            }


            playButton.setBackgroundResource(R.drawable.pause);
            pauseplay = false;

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    mp.start();
                    if(mTimer == null) {
                        mTimer = new Timer();
                        mTask = new LrcTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopLrcPlay(); // 停止歌词播放
                    mediaPlayer.stop(); // 停止当前的 MediaPlayer
                    mediaPlayer.release(); // 释放资源
                    mediaPlayer = new MediaPlayer(); // 将 mediaPlayer 对象置为 null
                    playNextSong(); // 播放下一首歌曲
                }

            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return true;
                }
            });
            // 更新歌曲标题文本
            if (songTitleTextView == null) {
                songTitleTextView = findViewById(R.id.song_title_text_view);
            }
            songTitleTextView.setText(song_urls.get(idx));

            // 获取随机背景颜色
            backgroundcolor = getRandomColor();
            int currentColor = Color.argb(alpha, Color.red(backgroundcolor), Color.green(backgroundcolor), Color.blue(backgroundcolor));
            GradientDrawable drawable = (GradientDrawable) layout.getBackground();
            drawable.setColors(new int[] {Color.WHITE, currentColor});
        } catch (Exception e) {
            Log.e("MP3View", "Error while preparing media player", e);
        }
    }

    private void showSongListDialog() {
        // 创建一个自定义布局
        dialogLayout = getLayoutInflater().inflate(R.layout.dialog_song_list, null);

        // 在自定义布局中找到 ListView
        ListView listView = dialogLayout.findViewById(R.id.song_list);

        // 设置适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, song_urls);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 用户点击了某首歌曲，播放该歌曲
                idx = position;
                playSelectedSong(song_urls.get(position));
            }
        });

        // 设置长按监听器
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteConfirmationDialog(position);
                return true;
            }
        });

        // 创建 AlertDialog 并设置自定义布局
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("歌曲列表");
        builder.setView(dialogLayout);

        // 显示对话框
        builder.show();
    }


    // 显示删除确认对话框
    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("是否删除该歌曲？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户确认删除，执行删除操作
                deleteSong(position);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 执行删除操作
    private void deleteSong(int position) {
        if (position >= 0 && position < song_urls.size()) {
            song_urls.remove(position);
            song_words_urls.remove(position);
            ListView listView = dialogLayout.findViewById(R.id.song_list); // 请根据实际情况获取 ListView
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    // 播放所选歌曲
    private void playSelectedSong(String selectedSong) {
        for (int i = 0; i < song_urls.size(); i++) {
            if (song_urls.get(i).equals(selectedSong)) {
                idx = i;
                playCurrentSong();
                break;
            }
        }
    }


    private void scanAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            // 获取 assets 文件夹中所有文件列表
            String[] files = assetManager.list("");

            // 遍历所有文件
            for (String file : files) {
                // 判断是否为歌曲文件
                if (file.endsWith(".mp3")) {
                    // 将符合条件的歌曲文件路径添加到歌曲列表中（添加 asset:// 前缀）
                    String assetFilePath = "asset://" + file;
                    song_urls.add(assetFilePath);

                    // 获取对应的歌词文件名
                    String lrcFile = file.substring(0, file.lastIndexOf(".")) + ".lrc";

                    // 将歌词文件名添加到歌词列表中
                    song_words_urls.add(lrcFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void toggleMute() {
        if (!isMuted) {
            // 静音
            previousVolume = audioManger.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioManger.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            isMuted = true;
            soundButton.setBackgroundResource(R.drawable.sound_shut);
        } else {
            // 恢复之前的音量
            audioManger.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
            isMuted = false;
            soundButton.setBackgroundResource(R.drawable.sound);
        }
    }
    private int calculateColor(int progress , int color) {
        alpha = (int) (255 * (progress / 100.0)) * 3 + 50; // 将进度转换为 alpha 值（0-255）
        return Color.argb(alpha,Color.red(color), Color.green(color), Color.blue(color)); // 红色，透明度根据进度变化
    }

    private int getRandomColor() {
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        if(backgroundcolor == color || color == Color.WHITE){
            return getRandomColor();
        }
        return color;
    }

    private void hideSystemUI() {
        // 隐藏状态栏和导航栏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public String getFromAssets(String fileName){
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName), "GBK");
            BufferedReader bufReader = new BufferedReader(inputReader);
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufReader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.append(line).append("\r\n");
                }
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息到控制台
            Log.e("MP3View", "Error while preparing media player", e);
            return ""; // 返回一个空字符串作为默认值
        }
    }


    class LrcTask extends TimerTask{
        @Override
        public void run() {
            //获取歌曲播放的位置
            final long timePassed = mediaPlayer.getCurrentPosition();
            MP3View.this.runOnUiThread(new Runnable() {
                public void run() {
                    //滚动歌词
                    mLrcView.seekLrcToTime(timePassed);
                }
            });

        }
    };


}


