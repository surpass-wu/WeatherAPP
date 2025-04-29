package com.example.weather.DB.LessonDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;

public class LessonDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_LESSON = "lesson";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME_RANGE = "time_range";

    // 数据库版本号
    private static final int DATABASE_VERSION = 1;
    // 数据库名称
    private static final String DATABASE_NAME = "lesson.db";

    // 创建表的 SQL 语句
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LESSON + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text not null, " + COLUMN_LOCATION
            + " text not null, " + COLUMN_DATE
            + " text not null, " + COLUMN_TIME_RANGE
            + " text not null);";

    public LessonDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 创建数据库表
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // 处理数据库版本升级
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // 查询数据库内容并输出到日志
    public void logDatabaseContent() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                "name",
                "location",
                "date",
                "time_range"
        };

        // 查询数据库中的信息
        // Cursor 是 Android 中用于遍历和访问数据库查询结果的接口。可以将其想象成一个“指针”，逐行指向数据库查询结果中的数据
        Cursor cursor = db.query(TABLE_LESSON, projection, null, null, null, null, null);

        // 遍历查询结果，并通过日志输出
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String timeRange = cursor.getString(cursor.getColumnIndexOrThrow("time_range"));

            Log.d("DatabaseContent", "Name: " + name + ", Location: " + location +
                    ", Date: " + date + ", Time Range: " + timeRange);
        }

        cursor.close();
        db.close();
    }
}


