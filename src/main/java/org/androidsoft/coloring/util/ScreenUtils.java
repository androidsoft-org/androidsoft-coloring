package org.androidsoft.coloring.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// Based on http://stackoverflow.com/questions/22265945/full-screen-action-bar-immersive#22560946
public class ScreenUtils {
    static public void setFullscreen(Activity act) {
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            act.getWindow().getDecorView().setSystemUiVisibility(
                      View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public static class StatusBarCollapser {

        private final Activity activity;
        private final WindowManager manager;
        private List<View> addedViews = new ArrayList<>();
        private boolean isInterceptingTheStatusBar = false;

        public StatusBarCollapser(Activity activity) {
            this.activity = activity;
            manager = ((WindowManager) this.activity.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));
            attachStatusBarListener();
        }

        private void attachStatusBarListener() {
            // see https://stackoverflow.com/a/31349378/1320237
            // see also https://stackoverflow.com/a/36101111/1320237
            // see also https://stackoverflow.com/a/47103959/1320237
            // see also https://stackoverflow.com/questions/32224452/android-unable-to-add-window-permission-denied-for-this-window-type#comment62111778_36101111
            int[] types = new int[]{
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.TYPE_PHONE,
            };
            int i = 0;
            for (int type : types) {
                WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
                localLayoutParams.type = type;
                localLayoutParams.gravity = Gravity.TOP;
                localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                        // this is to enable the notification to recieve touch events
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                        // Draws over status bar
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

                localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                //https://stackoverflow.com/questions/1016896/get-screen-dimensions-in-pixels
                int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
                int result = 0;
                if (resId > 0) {
                    result = activity.getResources().getDimensionPixelSize(resId);
                }

                localLayoutParams.height = result;

                localLayoutParams.format = PixelFormat.TRANSPARENT;

                StatusBarViewGroup view = new StatusBarViewGroup(this.activity);

                try {
                    manager.addView(view, localLayoutParams);
                    addedViews.add(view);
                } catch (Exception e2) {
                    // permission is not granted
                    Log.e("collapseStatusBar", "type: " + type + " at index: " + i);
                    e2.printStackTrace();
                }
                i++;
            }
            isInterceptingTheStatusBar = true;
        }

        private void detachStatusBarListener() {
            for (View view: addedViews) {
                manager.removeView(view);
            }
            isInterceptingTheStatusBar = false;
        }

        public void collapse() {
            try {
                // see https://stackoverflow.com/a/10380535/1320237
                @SuppressLint("WrongConstant") Object service = activity.getSystemService("statusbar");
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method collapse = statusbarManager.getMethod("collapse");
                collapse.setAccessible(true);
                collapse.invoke(service);
            } catch (Exception e) {
                // do not care - does not always work
            }
        }

        public void shouldCollapse() {
            attachStatusBarListener();
        }

        public void shouldNotCollapse() {
            detachStatusBarListener();
        }

        public class StatusBarViewGroup extends ViewGroup {

            public StatusBarViewGroup(Context context) {
                super(context);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                Log.v("StatusBarCollapser", "********** Intercepted Status Bar opening. Should collapse: " + isInterceptingTheStatusBar);
                return isInterceptingTheStatusBar;
            }
        }
    }

}
