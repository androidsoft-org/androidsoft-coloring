/*
 * Copyright (C) 2010 Peter Dornbach.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidsoft.coloring.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.androidsoft.coloring.ui.widget.PreCachingLayoutManager;
import org.androidsoft.coloring.util.ScreenUtils;
import org.androidsoft.coloring.util.Settings;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.images.ImageListener;
import org.androidsoft.coloring.util.images.SectionsAdapter;
import org.androidsoft.coloring.util.images.SettingsImageDB;
import org.androidsoft.utils.ui.NoTitleActivity;

import eu.quelltext.coloring.R;

public class ChoosePictureActivity extends NoTitleActivity
{

    public static final String RESULT_IMAGE = "image";
    public static final String ARG_IMAGE = "image";
    private ScreenUtils.StatusBarCollapser statusBar;
    // TODO: put gallery link in settings


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ScreenUtils.setFullscreen(this);
        statusBar = new ScreenUtils.StatusBarCollapser(this);
        // Apparently this cannot be set from the style.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.choose_picture);
        RecyclerView imagesView = findViewById(R.id.images);
        // load images ahead
        int space = getScreenHeight() / 2;
        LinearLayoutManager manager = new PreCachingLayoutManager(this, space);
        imagesView.setLayoutManager(manager);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        imagesView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        imagesView.setLayoutManager(layoutManager);

        // create a database with all the images
        SettingsImageDB imageDB = Settings.of(this).getImageDB();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(ARG_IMAGE)) {
            ImageDB.Image image = extras.getParcelable(ARG_IMAGE);
            if (image.canBePainted()) {
                imageDB.addPaintedImage(image);
            }
        }

        // set adapter with all the images
        SectionsAdapter adapter = new SectionsAdapter(
                imageDB, R.layout.choose_picture_line,
                new int[]{R.id.image1, R.id.image2});
        adapter.setImageListener(new ImageListener() {
            @Override
            public void onImageChosen(ImageDB.Image image) {
                returnImageToParent(image);
            }

        });
        imagesView.setAdapter(adapter);
    }

    private void returnImageToParent(ImageDB.Image image) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_IMAGE, image);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int getScreenHeight() {
        // from https://stackoverflow.com/a/4744499
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScreenUtils.setFullscreen(this);
        statusBar.shouldCollapse();
    }

    @Override
    protected void onPause() {
        super.onPause();
        statusBar.shouldNotCollapse();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            statusBar.collapse();
        }
    }
}
