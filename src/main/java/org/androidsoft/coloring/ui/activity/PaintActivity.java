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

import org.androidsoft.coloring.ui.widget.PaintArea;
import org.androidsoft.coloring.ui.widget.ColorButton;
import org.androidsoft.coloring.util.BitmapSaver;
import org.androidsoft.coloring.util.BitmapSharer;
import org.androidsoft.coloring.util.ScreenUtils;
import org.androidsoft.coloring.util.images.BitmapHash;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.images.ResourceImageDB;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import eu.quelltext.coloring.R;

public class PaintActivity extends AbstractColoringActivity
{

    private static final int REQUEST_CHOOSE_PICTURE = 0;
    private static final int REQUEST_PICK_COLOR = 1;
    private static final int DIALOG_PROGRESS = 1;
    public static final String ARG_IMAGE = "bitmap";
    private PaintArea paintArea;
    private ProgressDialog _progressDialog;
    // The ColorButtonManager makes sure the state of the ColorButtons visible
    // on this activity is in sync.
    private ColorButtonManager colorButtonManager;
    boolean doubleBackToExitPressedOnce = false;
    private BitmapSaver bitmapSaver = null;
    private int lastSavedHash; // the hash value of the last saved bitmap
    private ImageView paintView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.paint);
        paintView = (ImageView) findViewById(R.id.paint_view);
        paintArea = new PaintArea(paintView);
        colorButtonManager = new ColorButtonManager();
        View pickColorsButton = findViewById(R.id.pick_color_button);
        pickColorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaintActivity.this, PickColorActivity.class);
                startActivityForResult(intent, REQUEST_PICK_COLOR);
            }
        });

        // make the background area clickable
        final LinearLayout bg = findViewById(R.id.paint_view_background);
        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bg.setBackgroundColor(paintArea.getPaintColor());
            }
        });

        loadImageFromIntent(getIntent());
    }

    private void loadImageFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        ImageDB.Image image;
        if (extras != null && extras.containsKey(ARG_IMAGE)) {
            // we received and image and should thus paint it
            image = extras.getParcelable(ARG_IMAGE);
        } else {
            image = new ResourceImageDB().randomImage();
        }
        paintArea.setImageBitmap(image.getImage(PaintActivity.this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScreenUtils.setFullscreen(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // capture the new intent
        // see https://developer.android.com/guide/components/activities/tasks-and-back-stack
        // see https://developer.android.com/reference/android/app/Activity#onNewIntent(android.content.Intent)
        super.onNewIntent(intent);
        saveBitmap();
        loadImageFromIntent(intent);
    }

    @Override
    public void onBackPressed() {
        // code for double-clicking the back button to exit the activity
        // see https://stackoverflow.com/a/13578600/1320237
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.toast_double_click_back_button, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.paint_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.open_new:
                openPictureChoice();
                return true;
            case R.id.save:
                saveBitmap();
                return true;
            case R.id.about:
                startActivity(new Intent(INTENT_ABOUT));
                return true;
            case R.id.share:
                saveBitmap(new BitmapSharer(this, paintArea.getBitmap()));
                return true;
        }
        return false;
    }

    private void saveBitmap() {
        saveBitmap(new BitmapSaver(this, paintArea.getBitmap()));
    }

    private void saveBitmap(BitmapSaver newBitmapSaver) {
        int duration = Toast.LENGTH_SHORT;
        int message;
        String path;
        int newHash = BitmapHash.hash(newBitmapSaver.getBitmap());
        if (bitmapSaver != null && bitmapSaver.isRunning()) {
            // pressing save while in progress
            message = R.string.toast_save_file_running;
            path = bitmapSaver.getFile().getPath();
        } else if (lastSavedHash == newHash) {
            // image is already saved
            message = R.string.toast_save_file_again;
            path = bitmapSaver.getFile().getPath();
            newBitmapSaver.alreadySaved(bitmapSaver);
        } else {
            // image is not saved
            bitmapSaver = newBitmapSaver;
            bitmapSaver.start();
            message = R.string.toast_save_file;
            path = bitmapSaver.getFile().getName();
            lastSavedHash = BitmapHash.hash(newBitmapSaver.getBitmap());
        }
        // create a toast
        // see https://developer.android.com/guide/topics/ui/notifiers/toasts#java
        String text = getString(message, path);
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

    }

    private void openPictureChoice() {
        // how to start a new activity
        // see https://stackoverflow.com/a/4186097
        Intent intent = new Intent(this, ChoosePictureActivity.class);
        ImageDB.Image image = paintArea.getImage();
        intent.putExtra(ChoosePictureActivity.ARG_IMAGE, image);
        startActivityForResult(intent, REQUEST_CHOOSE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CHOOSE_PICTURE:
                if (resultCode == RESULT_OK)
                {
                    final ImageDB.Image image = data.getParcelableExtra(ChoosePictureActivity.RESULT_IMAGE);
                    paintArea.setImageBitmap(image.getImage(PaintActivity.this));
                }
                break;
            case REQUEST_PICK_COLOR:
                if (resultCode != RESULT_CANCELED)
                {
                    colorButtonManager.selectColor(resultCode);
                }
                break;
        }
    }

    private class ColorButtonManager implements View.OnClickListener
    {

        public ColorButtonManager()
        {
            findAllColorButtons(_colorButtons);
            _usedColorButtons.addAll(_colorButtons);
            _selectedColorButton = _usedColorButtons.getFirst();
            _selectedColorButton.setSelected(true);
            Iterator<ColorButton> i = _usedColorButtons.iterator();
            while (i.hasNext())
            {
                i.next().setOnClickListener(ColorButtonManager.this);
            }
            setPaintViewColor();
        }

        public void onClick(View view)
        {
            if (view instanceof ColorButton)
            {
                selectButton((ColorButton) view);
            }
        }

        // Select the button that has the given color, or if there is no such
        // button then set the least recently used button to have that color.
        public void selectColor(int color)
        {
            _selectedColorButton = selectAndRemove(color);
            if (_selectedColorButton == null)
            {
                // Recycle the last used button to hold the new color.
                _selectedColorButton = _usedColorButtons.removeLast();
                _selectedColorButton.setColor(color);
                _selectedColorButton.setSelected(true);
            }
            _usedColorButtons.addFirst(_selectedColorButton);
            setPaintViewColor();
        }

        // Select the given button.
        private void selectButton(ColorButton button)
        {
            _selectedColorButton = selectAndRemove(button.getColor());
            _usedColorButtons.addFirst(_selectedColorButton);
            setPaintViewColor();
        }

        // Set the currently selected color in the paint view.
        private void setPaintViewColor()
        {
            int selectedColor = _selectedColorButton.getColor();
            paintArea.setPaintColor(selectedColor);
            setBackgroundColorOfButtons(selectedColor);
        }

        private void setBackgroundColorOfButtons(int color) {
            int backgroundColor = (color & 0xffffff) | 0x70000000;
            View buttonHolder = findViewById(R.id.color_buttons_container);
            buttonHolder.setBackgroundColor(backgroundColor);
        }

        // Finds the button with the color. If found, sets it to selected,
        // removes it and returns it. If not found, it returns null. All
        // other buttons are unselected.
        private ColorButton selectAndRemove(int color)
        {
            ColorButton result = null;
            Iterator<ColorButton> i = _usedColorButtons.iterator();
            while (i.hasNext())
            {
                ColorButton b = i.next();
                if (b.getColor() == color)
                {
                    result = b;
                    b.setSelected(true);
                    i.remove();
                }
                else
                {
                    b.setSelected(false);
                }
            }
            return result;
        }
        // A list of pointers to all buttons in the order
        // in which they have been used.
        private List<ColorButton> _colorButtons = new ArrayList<ColorButton>();
        private LinkedList<ColorButton> _usedColorButtons = new LinkedList<ColorButton>();
        private ColorButton _selectedColorButton;
    }
}