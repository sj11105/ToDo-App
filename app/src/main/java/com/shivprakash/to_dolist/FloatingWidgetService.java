package com.shivprakash.to_dolist;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class FloatingWidgetService extends Service {

    private WindowManager windowManager;
    private View floatingWidget;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Inflate the floating widget layout
        floatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);

        // Configure layout parameters
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        // Position of the widget
        params.x = 0;
        params.y = 100;

        // Add view to the WindowManager
        windowManager.addView(floatingWidget, params);

        // Handle touch events to move the widget
        floatingWidget.findViewById(R.id.closeButton).setOnClickListener(v -> stopSelf());

        floatingWidget.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingWidget, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingWidget != null) {
            windowManager.removeView(floatingWidget);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
