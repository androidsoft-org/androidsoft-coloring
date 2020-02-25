package org.androidsoft.coloring.util;

import android.app.Activity;
import android.view.View;

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
}
