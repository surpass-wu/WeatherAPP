package com.example.weather.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.DB.LessonDB.LessonDBHelper;
import com.example.weather.R;
import com.google.android.material.card.MaterialCardView;
import java.util.Calendar;
import java.util.Locale;

// 课程表管理界面
public class SchActivity extends AppCompatActivity {

    private TextView scheduleTextView;
    private LinearLayout lessonContainer;

    private Calendar calendar;
    private LessonDBHelper dbHelper;
    private LinearLayout addCourseLayout;

    private CalendarView calendarView;
    private int dayOfWeek , dayOfMonth , year , month;

    private Button backButton , lastdayBtn , nextdayBtn , setButton;

    // 初始化整个界面，设置按钮点击事件和日历选择监听
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sch);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        scheduleTextView = findViewById(R.id.schedule_text_view);
        addCourseLayout = findViewById(R.id.add_course_layout);
        lessonContainer = findViewById(R.id.lesson_container);

        calendarView = findViewById(R.id.calender);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                SchActivity.this.year = year;
                SchActivity.this.month = month + 1;
                SchActivity.this.dayOfMonth = dayOfMonth;

                Calendar selectedCalendar = Calendar.getInstance();
                SchActivity.this.calendar = selectedCalendar;
                selectedCalendar.set(year, month, dayOfMonth);
                int dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);
                SchActivity.this.dayOfWeek = dayOfWeek;

                String[] weekdays = new String[]{"", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
                String dayOfWeekText = weekdays[dayOfWeek];
                String selectedDate = String.format(Locale.getDefault(), "%d年%d月%d日 %s", year, month + 1, dayOfMonth, dayOfWeekText);
                scheduleTextView.setText(selectedDate);

                // Update the database based on the selected date
                loadTodayLessons();
            }

        });
        calendar = Calendar.getInstance();
        dbHelper = new LessonDBHelper(this);
        setButton = findViewById(R.id.setting);

        lastdayBtn = findViewById(R.id.last_day);
        nextdayBtn = findViewById(R.id.next_day);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lastdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 将日期往前退一天
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                calendarView.setDate(calendar.getTimeInMillis(), true, true);
                String todaySchedule = getTodaySchedule();
                scheduleTextView.setText(todaySchedule);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                loadTodayLessons();
            }
        });

        nextdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 将日期前进一天
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                calendarView.setDate(calendar.getTimeInMillis(), true, true);
                String todaySchedule = getTodaySchedule();
                scheduleTextView.setText(todaySchedule);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH) + 1;
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                loadTodayLessons();
            }
        });


        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mySuperIntent;
                mySuperIntent = new Intent(SchActivity.this, SetSchActivity.class);
                startActivity(mySuperIntent);
            }
        });

        addCourseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddLessonClick(v);
            }
        });

        String todaySchedule = getTodaySchedule();
        scheduleTextView.setText(todaySchedule);

        // 加载今日课程
        loadTodayLessons();
    }

    // 生成当前日期的文字描述（如“2023年10月5日 星期三”）
    private String getTodaySchedule() {
        // 获取今日日期
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        String[] weekdays = new String[]{"", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        String dayOfWeekText = weekdays[dayOfWeek];

        return String.format(Locale.getDefault(), "%d年%d月%d日 %s", year, month, dayOfMonth, dayOfWeekText);
    }

    // 从数据库加载用户选择的日期的课程，并显示在界面上
    private void loadTodayLessons() {
        lessonContainer.removeAllViews(); // 清空之前加载的课程

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询今日课程
        Cursor cursor = db.query(
                LessonDBHelper.TABLE_LESSON,
                null,
                LessonDBHelper.COLUMN_DATE + " = ?",
                new String[]{ year + "-" + month + "-" + dayOfMonth},
                null,
                null,
                null
        );

        // 遍历查询结果，添加到界面中
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(LessonDBHelper.COLUMN_NAME));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(LessonDBHelper.COLUMN_LOCATION));
            String timeRange = cursor.getString(cursor.getColumnIndexOrThrow(LessonDBHelper.COLUMN_TIME_RANGE));

            Log.i("LessonInfo", "课程名称：" + name + ", 地点：" + location + ", 时间范围：" + timeRange);

            View lessonView = LayoutInflater.from(this).inflate(R.layout.sch_manage_item, null);
            lessonView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 弹出 AlertDialog 显示课程信息并提供修改和删除选项
                    showEditDialog(name, location, timeRange);
                    return true;
                }
            });
            TextView lessonName = lessonView.findViewById(R.id.lessonName);
            TextView placeLocation = lessonView.findViewById(R.id.placeLocation);
            TextView lessonTime = lessonView.findViewById(R.id.lessonTime);

            lessonName.setText(name);
            placeLocation.setText(location);
            lessonTime.setText(timeRange);

            lessonContainer.addView(lessonView);
        }

        cursor.close();
        db.close();
    }

    // 弹出 AlertDialog 显示课程信息并提供修改和删除选项
    private void showEditDialog(String name, String location, String timeRange) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_lesson, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.edit_text_name);
        EditText locationEditText = dialogView.findViewById(R.id.edit_text_location);
        EditText timeRangeEditText = dialogView.findViewById(R.id.edit_text_time_range);

        nameEditText.setText(name);
        locationEditText.setText(location);
        timeRangeEditText.setText(timeRange);

        builder.setPositiveButton("修改", (dialog, which) -> {
            // 执行修改课程信息的逻辑
            String newName = nameEditText.getText().toString();
            String newLocation = locationEditText.getText().toString();
            String newTimeRange = timeRangeEditText.getText().toString();
            // 调用方法更新数据库中的课程信息
            updateLessonInDatabase(name, newName, newLocation, newTimeRange);
        });

        builder.setNegativeButton("删除", (dialog, which) -> {
            // 执行删除课程信息的逻辑
            deleteLessonFromDatabase(name);
        });

        builder.setNeutralButton("取消", null);
        builder.create().show();
    }

    // 更新数据库中的课程信息
    private void updateLessonInDatabase(String oldName, String newName, String newLocation, String newTimeRange) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LessonDBHelper.COLUMN_NAME, newName);
        values.put(LessonDBHelper.COLUMN_LOCATION, newLocation);
        values.put(LessonDBHelper.COLUMN_TIME_RANGE, newTimeRange);

        // 执行更新操作
        int rowsAffected = db.update(LessonDBHelper.TABLE_LESSON, values, LessonDBHelper.COLUMN_NAME + " = ?", new String[]{oldName});
        db.close();

        if (rowsAffected > 0) {
            // 更新成功，重新加载今日课程
            loadTodayLessons();
            Toast.makeText(this, "课程信息已更新", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "课程信息更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 删除数据库中的课程信息
    private void deleteLessonFromDatabase(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 执行删除操作
        int rowsAffected = db.delete(LessonDBHelper.TABLE_LESSON, LessonDBHelper.COLUMN_NAME + " = ?", new String[]{name});
        db.close();

        if (rowsAffected > 0) {
            // 删除成功，重新加载今日课程
            loadTodayLessons();
            Toast.makeText(this, "课程删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "课程删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 点击“+”按钮时，弹出对话框让用户填写新课程信息，并保存到数据库。
    public void onAddLessonClick(View view) {
        // AlertDialog：Android 系统提供的弹窗组件，用于显示临时界面（比如提示、输入框等）
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_lesson, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.edit_text_name);
        EditText locationEditText = dialogView.findViewById(R.id.edit_text_location);
        EditText dateEditText = dialogView.findViewById(R.id.edit_text_date);
        EditText timeRangeEditText = dialogView.findViewById(R.id.edit_text_time_range);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String location = locationEditText.getText().toString();
            String date = dateEditText.getText().toString();
            String timeRange = timeRangeEditText.getText().toString();

            if (!name.isEmpty() && !location.isEmpty() && !date.isEmpty() && !timeRange.isEmpty()) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(LessonDBHelper.COLUMN_NAME, name);
                values.put(LessonDBHelper.COLUMN_LOCATION, location);
                values.put(LessonDBHelper.COLUMN_DATE, date);
                values.put(LessonDBHelper.COLUMN_TIME_RANGE, timeRange);

                long newRowId = db.insert(LessonDBHelper.TABLE_LESSON, null, values);
                if (newRowId != -1) {
                    Toast.makeText(this, "课程添加成功", Toast.LENGTH_SHORT).show();
                    loadTodayLessons(); // 加载新添加的课程
                } else {
                    Toast.makeText(this, "课程添加失败", Toast.LENGTH_SHORT).show();
                }

                db.close();
            } else {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.create().show();
    }
}
