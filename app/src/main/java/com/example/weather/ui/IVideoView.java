package com.example.weather.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

// 自定义的视频播放器
public class IVideoView extends VideoView {
    public IVideoView(Context context) {
        super(context);
    }

    public IVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 自动适应屏幕，避免拉伸变形
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(0,widthMeasureSpec);
        int height = getDefaultSize(0,heightMeasureSpec);
        setMeasuredDimension(width,height);
    }
}
// webview浏览器自己加载
// 从网页爬数据，查完数据交给逻辑层存储为本地数据库
// UI还可根据用户喜好变色等。     xml命令某个颜色，颜色转变