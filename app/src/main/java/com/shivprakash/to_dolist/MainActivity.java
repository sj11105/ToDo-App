package com.shivprakash.to_dolist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    private TaskDBHelper dbHelper;
    private List<Data> taskData;
    FloatingActionButton fabAddTask;
    RecyclerView recyclerView;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskData = new ArrayList<>();
        dbHelper = new TaskDBHelper(this);

        loadTasksFromSQLite(taskData);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskData);
        recyclerView.setAdapter(adapter);

        fabAddTask = findViewById(R.id.fab_add_task);

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        // Handle task actions
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Intent intent = new Intent(MainActivity.this, editTask.class);
                intent.putExtra("task", taskData.get(position).getName());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                markTaskAsComplete(position);
                taskData.remove(position);
                Toast.makeText(MainActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onCheckboxClick(int position) {
                markTaskAsComplete(position);
                taskData.remove(position);
                Toast.makeText(MainActivity.this, "Task Completed", Toast.LENGTH_SHORT).show();
                adapter.notifyItemRemoved(position);
            }
        });

        // Request overlay permission
        checkOverlayPermission();
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        } else {
            startFloatingWidgetService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startFloatingWidgetService();
            } else {
                Toast.makeText(this, "Overlay permission is required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startFloatingWidgetService() {
        Intent serviceIntent = new Intent(this, FloatingWidgetService.class);
        startService(serviceIntent);
    }

    public void loadTasksFromSQLite(List<Data> data) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME, null);

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK));
            @SuppressLint("Range") String taskDate = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            @SuppressLint("Range") String taskTime = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY));
            @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY));
            @SuppressLint("Range") String notes = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NOTES));

            data.add(new Data(taskName, taskDate, taskTime, category, priority, notes));
        }

        cursor.close();
        db.close();
    }

    public void markTaskAsComplete(int position) {
        String task = taskData.get(position).getName();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?", new String[]{task});
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    public class Data {
        String name;
        String date;
        String time;
        String category;
        String priority;
        String notes;

        Data(String name, String date, String time, String category, String priority, String notes) {
            this.name = name;
            this.date = date;
            this.time = time;
            this.category = category;
            this.priority = priority;
            this.notes = notes;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getCategory() {
            return category;
        }

        public String getPriority() {
            return priority;
        }

        public String getNotes() {
            return notes;

        }
    }
}
