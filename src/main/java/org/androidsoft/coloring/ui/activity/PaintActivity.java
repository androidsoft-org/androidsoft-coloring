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

import org.androidsoft.coloring.ui.widget.PaintView;
import org.androidsoft.coloring.ui.widget.ColorButton;
import org.androidsoft.coloring.ui.widget.Progress;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import java.util.List;
import java.util.ArrayList;
import org.androidsoft.coloring.R;

public class PaintActivity extends AbstractColoringActivity implements
        PaintView.LifecycleListener
{

    public static final String FILE_BACKUP = "backup";

    public PaintActivity()
    {
        _state = new State();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.paint);
        _paintView = (PaintView) findViewById(R.id.paint_view);
        _paintView.setLifecycleListener(this);
        _progressBar = (ProgressBar) findViewById(R.id.paint_progress);
        _progressBar.setMax(Progress.MAX);
        _colorButtonManager = new ColorButtonManager();
        View pickColorsButton = findViewById(R.id.pick_color_button);
        pickColorsButton.setOnClickListener(new PickColorListener());

        final Object previousState = getLastNonConfigurationInstance();
        if (previousState == null)
        {
            // No previous state, this is truly a new activity.
            // We need to make the paint view INVISIBLE (and not GONE) so that
            // it can measure itself correctly.
            _paintView.setVisibility(View.INVISIBLE);
            _progressBar.setVisibility(View.GONE);
        }
        else
        {
            // We have a previous state, so this is a re-created activity.
            // Restore the state of the activity.
            SavedState state = (SavedState) previousState;
            _state = state._paintActivityState;
            _paintView.setState(state._paintViewState);
            _colorButtonManager.setState(state._colorButtonState);
            _paintView.setVisibility(View.VISIBLE);
            _progressBar.setVisibility(View.GONE);
            if (_state._loadInProgress)
            {
                new InitPaintView(_state._loadedResourceId);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.paint_menu, menu);
        return true;
    }

    public void onPreparedToLoad()
    {
        // We need to invoke InitPaintView in a callback otherwise
        // the visibility changes do not seem to be effective.
        new Handler()
        {

            @Override
            public void handleMessage(Message m)
            {
                new InitPaintView(StartNewActivity.randomOutlineId());
            }
        }.sendEmptyMessage(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.open_new:
                startActivityForResult(new Intent(INTENT_START_NEW), REQUEST_START_NEW);
                return true;
            case R.id.save:
                new BitmapSaver();
                return true;
            case R.id.about:
                startActivity(new Intent(INTENT_ABOUT));
                return true;
            case R.id.share:
                new BitmapSharer();
                return true;
        }
        return false;
    }


    @Override
    public Object onRetainNonConfigurationInstance()
    {
        SavedState state = new SavedState();
        state._paintActivityState = _state;
        state._paintViewState = _paintView.getState();
        state._colorButtonState = _colorButtonManager.getState();
        return state;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_START_NEW:
                if (resultCode != 0)
                {
                    new InitPaintView(resultCode);
                }
                break;
            case REQUEST_PICK_COLOR:
                if (resultCode != 0)
                {
                    _colorButtonManager.selectColor(resultCode);
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

    private class PickColorListener implements View.OnClickListener
    {

        public void onClick(View view)
        {
            startActivityForResult(new Intent(INTENT_PICK_COLOR), REQUEST_PICK_COLOR);
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

        public Object getState()
        {
            int[] result = new int[_colorButtons.size() + 1];
            int n = _colorButtons.size();
            for (int i = 0; i < n; i++)
            {
                result[i] = _colorButtons.get(i).getColor();
            }
            result[n] = _selectedColorButton.getColor();
            return result;
        }

        public void setState(Object o)
        {
            int[] state = (int[]) o;
            int n = _colorButtons.size();
            for (int i = 0; i < n; i++)
            {
                _colorButtons.get(i).setColor(state[i]);
            }
            selectColor(state[n]);
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
            _paintView.setPaintColor(_selectedColorButton.getColor());
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

    private class InitPaintView implements Runnable
    {

        public InitPaintView(int outlineResourceId)
        {
            // Make the progress bar visible and hide the view
            _paintView.setVisibility(View.GONE);
            _progressBar.setProgress(0);
            _progressBar.setVisibility(View.VISIBLE);
            _state._savedImageUri = null;

            _state._loadInProgress = true;
            _state._loadedResourceId = outlineResourceId;
            _originalOutlineBitmap = BitmapFactory.decodeResource(getResources(),
                    outlineResourceId);
            _handler = new Handler()
            {

                @Override
                public void handleMessage(Message m)
                {
                    switch (m.what)
                    {
                        case Progress.MESSAGE_INCREMENT_PROGRESS:
                            // Update progress bar.
                            _progressBar.incrementProgressBy(m.arg1);
                            break;
                        case Progress.MESSAGE_DONE_OK:
                        case Progress.MESSAGE_DONE_ERROR:
                            // We are done, hide the progress bar and turn 
                            // the paint view back on.
                            _state._loadInProgress = false;
                            _paintView.setVisibility(View.VISIBLE);
                            _progressBar.setVisibility(View.GONE);
                            break;
                    }
                }
            };

            new Thread(this).start();
        }

        public void run()
        {
            _paintView.loadFromBitmap(_originalOutlineBitmap, _handler);
        }
        private Bitmap _originalOutlineBitmap;
        private Handler _handler;
    }

    // Class needed to work-around gallery crash bug. If we do not have this
    // scanner then the save succeeds but the Pictures app will crash when
    // trying to open.
    private class MediaScannerNotifier implements MediaScannerConnectionClient
    {

        public MediaScannerNotifier(Context context, String path, String mimeType)
        {
            _path = path;
            _mimeType = mimeType;
            _connection = new MediaScannerConnection(context, this);
            _connection.connect();
        }

        public void onMediaScannerConnected()
        {
            _connection.scanFile(_path, _mimeType);
        }

        public void onScanCompleted(String path, final Uri uri)
        {
            _connection.disconnect();
        }
        private MediaScannerConnection _connection;
        private String _path;
        private String _mimeType;
    }

    public static interface OnSavedListener
    {

        void onSaved(String filename);
    }

    private class BitmapSaver implements Runnable
    {

        public BitmapSaver()
        {
            class DelayHandler extends Handler
            {

                @Override
                public void handleMessage(Message m)
                {
                    // We are done, hide the progress bar and turn 
                    // the paint view back on.
                    _saveInProgress = false;
                    _progressDialog.dismiss();
                }
            }

            class ProgressHandler extends Handler
            {

                @Override
                public void handleMessage(Message m)
                {
                    switch (m.what)
                    {
                        case Progress.MESSAGE_INCREMENT_PROGRESS:
                            // Update progress bar.
                            _progressDialog.incrementProgressBy(m.arg1);
                            break;
                        case Progress.MESSAGE_DONE_OK:
                        case Progress.MESSAGE_DONE_ERROR:
                            if (m.what == Progress.MESSAGE_DONE_OK)
                            {
                                finishSaving();
                            }
                            String title = getString(R.string.dialog_saving);
                            if (m.what == Progress.MESSAGE_DONE_OK)
                            {
                                title += getString(R.string.dialog_saving_ok);
                            }
                            else
                            {
                                title += getString(R.string.dialog_saving_error);
                            }
                            _progressDialog.setTitle(title);
                            new DelayHandler().sendEmptyMessageDelayed(0,
                                    SAVE_DIALOG_WAIT_MILLIS);
                            break;
                    }
                }
            }

            if (_paintView.isInitialized())
            {
                _saveInProgress = true;
                showDialog(DIALOG_PROGRESS);
                _progressDialog.setTitle(R.string.dialog_saving);
                _progressDialog.setProgress(0);
                _originalOutlineBitmap = BitmapFactory.decodeResource(getResources(),
                        _state._loadedResourceId);
                _progressHandler = new ProgressHandler();
                new Thread(this).start();
            }
        }

        public void run()
        {
            // Get a filename.
            _fileName = newImageFileName();
            _file = new File(Environment.getExternalStorageDirectory(),
                    getString(R.string.saved_image_path_prefix) + _fileName + ".png");

            // Save the bitmap to a file.
            _paintView.saveToFile(_file, _originalOutlineBitmap, _progressHandler);
        }

        protected void finishSaving()
        {
            // Save it to the MediaStore.
            ContentValues values = new ContentValues();
            values.put(Images.Media.TITLE, _fileName);
            values.put(Images.Media.DISPLAY_NAME, _fileName);
            values.put(Images.Media.MIME_TYPE, MIME_PNG);
            values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(Images.Media.DATA, _file.toString());
            File parentFile = _file.getParentFile();
            values.put(Images.Media.BUCKET_ID,
                    parentFile.toString().toLowerCase().hashCode());
            values.put(Images.Media.BUCKET_DISPLAY_NAME,
                    parentFile.getName().toLowerCase());
            _newImageUri = getContentResolver().insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values);

            // Delete the old version, if we have any.
            if (_state._savedImageUri != null)
            {
                getContentResolver().delete(_state._savedImageUri, null, null);
            }
            _state._savedImageUri = _newImageUri;

            // Scan the file so that it appears in the system as it should.
            if (_newImageUri != null)
            {
                new MediaScannerNotifier(PaintActivity.this, _file.toString(), MIME_PNG);
            }
        }

        private String newImageFileName()
        {
            final DateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
            return fmt.format(new Date());
        }
        private Bitmap _originalOutlineBitmap;
        private String _fileName;
        private File _file;
        private Handler _progressHandler;
        protected Uri _newImageUri;
    }

    private class BitmapSharer extends BitmapSaver
    {

        public BitmapSharer()
        {
            super();
        }

        @Override
        protected void finishSaving()
        {
            super.finishSaving();

            if (_newImageUri != null)
            {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/png");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, _newImageUri);
                startActivity(Intent.createChooser(sharingIntent, getString( R.string.dialog_share )));
            }
        }
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
        public int _loadedResourceId;
        // If we have already saved a copy of the image, we store the URI here
        // so that we can delete the previous version when saved again.
        public Uri _savedImageUri;
    }
    private static final int REQUEST_START_NEW = 0;
    private static final int REQUEST_PICK_COLOR = 1;
    private static final int DIALOG_PROGRESS = 1;
    private static final int SAVE_DIALOG_WAIT_MILLIS = 1500;
    private static final String MIME_PNG = "image/png";
    // The state that we will carry over if the activity is recreated.
    private State _state;
    // Main UI elements.
    private PaintView _paintView;
    private ProgressBar _progressBar;
    private ProgressDialog _progressDialog;
    // The ColorButtonManager makes sure the state of the ColorButtons visible
    // on this activity is in sync.
    private ColorButtonManager _colorButtonManager;
    // Is there a save in progress?
    private boolean _saveInProgress;
}