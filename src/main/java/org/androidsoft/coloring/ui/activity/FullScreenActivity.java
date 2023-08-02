package org.androidsoft.coloring.ui.activity;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;

import org.androidsoft.coloring.util.ScreenUtils;
import org.androidsoft.utils.ui.NoTitleActivity;

public class FullScreenActivity extends NoTitleActivity {

    private ScreenUtils.StatusBarCollapser statusBar = null;

    @Override
    protected void onResume() {
        super.onResume();
        ScreenUtils.setFullscreen(this);
        getStatusBar().shouldCollapse();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getStatusBar().shouldNotCollapse();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            getStatusBar().collapse();
        }
    }

    public ScreenUtils.StatusBarCollapser getStatusBar() {
        if (statusBar == null) {
            statusBar = new ScreenUtils.StatusBarCollapser(this);
        }
        return statusBar;
    }
}
