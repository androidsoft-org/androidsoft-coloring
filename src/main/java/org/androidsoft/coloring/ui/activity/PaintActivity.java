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
import org.androidsoft.coloring.ui.widget.Progress;
import org.androidsoft.coloring.util.BitmapSaver;
import org.androidsoft.coloring.util.BitmapSharer;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.images.ResourceImageDB;

import java.util.Iterator;
import java.util.LinkedList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    // The state that we will carry over if the activity is recreated.
    private State _state;
    // Main UI elements.
    private PaintArea paintArea;
    private ProgressBar _progressBar;
    private ProgressDialog _progressDialog;
    // The ColorButtonManager makes sure the state of the ColorButtons visible
    // on this activity is in sync.
    private ColorButtonManager colorButtonManager;
    // Is there a save in progress?
    private boolean _saveInProgress;
    boolean doubleBackToExitPressedOnce = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.paint);
        paintArea = new PaintArea((ImageView) findViewById(R.id.paint_view));
        colorButtonManager = new ColorButtonManager();
        View pickColorsButton = findViewById(R.id.pick_color_button);
        pickColorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaintActivity.this, PickColorActivity.class);
                startActivityForResult(intent, REQUEST_PICK_COLOR);
            }
        });

        loadFromArguments();
    }

    private void loadFromArguments() {
        Bundle extras = getIntent().getExtras();
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
                new BitmapSaver(this, paintArea.getBitmap()).start();
                return true;
            case R.id.about:
                startActivity(new Intent(INTENT_ABOUT));
                return true;
            case R.id.share:
                new BitmapSharer(this, paintArea.getBitmap()).start();
                return true;
        }
        return false;
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
                    ImageDB.Image image = data.getParcelableExtra(ChoosePictureActivity.RESULT_IMAGE);
                    paintArea.setImageBitmap(image.getImage(this));
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

    // @Override
    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DIALOG_PROGRESS:
                _progressDialog = new ProgressDialog(PaintActivity.this);
                _progressDialog.setCancelable(false);
                _progressDialog.setIcon(android.R.drawable.ic_dialog_info);
                _progressDialog.setTitle(R.string.dialog_saving);
                _progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                _progressDialog.setMax(Progress.MAX);
                if (!_saveInProgress)
                {
                    // This means that the view hierarchy was recreated but there
                    // is no actual save in progress (in this hierarchy), so let's
                    // dismiss the dialog.
                    new Handler()
                    {

                        @Override
                        public void handleMessage(Message m)
                        {
                            _progressDialog.dismiss();
                        }
                    }.sendEmptyMessage(0);
                }

                return _progressDialog;
        }
        return null;
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
            paintArea.setPaintColor(_selectedColorButton.getColor());
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

    // The state of the whole drawing. This is used to transfer the state if
    // the activity is re-created (e.g. due to orientation change).
    private static class SavedState
    {

        public State _paintActivityState;
        public Object _colorButtonState;
        public Object _paintViewState;
    }

    private static class State
    {
        // Are we just loading a new outline?

        public boolean _loadInProgress;
        // The resource ID of the outline we are coloring.
        public Bitmap _loadedBitmap;
        // If we have already saved a copy of the image, we store the URI here
        // so that we can delete the previous version when saved again.
        public Uri _savedImageUri;
    }

}