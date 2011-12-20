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
package org.androidsoft.coloring.ui.widget;

import org.androidsoft.coloring.util.FloodFill;
import org.androidsoft.coloring.util.DrawUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.FileOutputStream;

public class PaintView extends View
{

    public interface LifecycleListener
    {
        // After this method it is allowed to load resources.

        public void onPreparedToLoad();
    }

    public PaintView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _state = new State();
        _paint = new Paint();
        
    }

    public PaintView(Context context)
    {
        this(context, null);
    }

    public synchronized void setLifecycleListener(LifecycleListener l)
    {
        _lifecycleListener = l;
        
    }

    public synchronized Object getState()
    {
        return _state;
    }

    public synchronized void setState(Object o)
    {
        _state = (State) o;
    }

    public void loadFromBitmap(Bitmap originalOutlineBitmap,
            Handler progressHandler)
    {
        // Proportion of progress in various places.
        // The sum of all progress should be 100.
        final int PROGRESS_RESIZE = 10;
        final int PROGRESS_SCAN = 90;

        int w = 0;
        int h = 0;
        State newState = new State();
        synchronized (this)
        {
            w = _state._width;
            h = _state._height;
            newState._color = _state._color;
            newState._width = w;
            newState._height = h;
        }
        final int n = w * h;

        // Resize so that it matches our paint size.
        Bitmap resizedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        DrawUtils.convertSizeClip(originalOutlineBitmap, resizedBitmap);
        Progress.sendIncrementProgress(progressHandler, PROGRESS_RESIZE);

        // Scan through the bitmap. We create the "outline" bitmap that is
        // completely black and has the alpha channel set only. We also
        // create the "mask" that we will use later when filling.
        newState._outlineBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        newState._paintMask = new byte[n];
        {
            int pixels[] = new int[n];
            resizedBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i2 = 0; i2 < PROGRESS_SCAN; i2++)
            {
                final int iStart = i2 * n / PROGRESS_SCAN;
                final int iEnd = (i2 + 1) * n / PROGRESS_SCAN;
                for (int i = iStart; i < iEnd; i++)
                {
                    int alpha = 255 - DrawUtils.brightness(pixels[i]);
                    newState._paintMask[i] = (alpha < ALPHA_TRESHOLD ? (byte) 1
                            : (byte) 0);
                    pixels[i] = alpha << 24;
                }
                Progress.sendIncrementProgress(progressHandler, 1);
            }
            newState._outlineBitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        }

        // Initialize the rest.
        newState._paintedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        newState._paintedBitmap.eraseColor(Color.WHITE);
        newState._workingMask = new byte[n];
        newState._pixels = new int[n];
        Arrays.fill(newState._pixels, Color.WHITE);

        // Commit our changes. So far we have only worked on local variables
        // so we only synchronize now.
        synchronized (this)
        {
            _state = newState;
        }
        progressHandler.sendEmptyMessage(Progress.MESSAGE_DONE_OK);
    }

    public synchronized void saveToFile(File file, Bitmap originalOutlineBitmap,
            Handler progressHandler)
    {
        // Proportion of progress in various places.
        // The sum of all progress should be 100.
        final int PROGRESS_SCAN_PAINTED = 25;
        final int PROGRESS_DRAW_PAINTED = 5;
        final int PROGRESS_SCAN_OUTLINE = 45;
        final int PROGRESS_DRAW_OUTLINE = 10;
        final int PROGRESS_SAVE = 15;

        // First, get a copy of the painted bitmap. After that we do not have
        // to deal with class instance any more.
        Bitmap painted;
        // synchronized (this) // already synchronized
        {
            painted = _state._paintedBitmap.copy(_state._paintedBitmap.getConfig(),
                    true);
        }
        // Now, scan over the original painted bitmap to "extend" the painted
        // regions by one pixel to fix accidental white areas because of resizing.
        {
            final int hp = painted.getHeight();
            final int wp = painted.getWidth();
            final int np = hp * wp;
            int[] origPixels = new int[np];
            int[] newPixels = new int[np];
            painted.getPixels(newPixels, 0, wp, 0, 0, wp, hp);
            System.arraycopy(newPixels, 0, origPixels, 0, np);
            for (int y2 = 0; y2 < PROGRESS_SCAN_PAINTED; y2++)
            {
                final int yStart = y2 * hp / PROGRESS_SCAN_PAINTED;
                final int yEnd = (y2 + 1) * hp / PROGRESS_SCAN_PAINTED;
                int p = yStart * wp;
                for (int y = yStart; y < yEnd; y++)
                {
                    for (int x = 0; x < wp; x++)
                    {
                        if (origPixels[p] == Color.WHITE)
                        {
                            if (x > 0 && origPixels[p - 1] != Color.WHITE)
                            {
                                newPixels[p] = origPixels[p - 1];
                            }
                            else if (y > 0 && origPixels[p - wp] != Color.WHITE)
                            {
                                newPixels[p] = origPixels[p - wp];
                            }
                            else if (x < wp - 1 && origPixels[p + 1] != Color.WHITE)
                            {
                                newPixels[p] = origPixels[p + 1];
                            }
                            else if (y < hp - 1 && origPixels[p + wp] != Color.WHITE)
                            {
                                newPixels[p] = origPixels[p + wp];
                            }
                        }
                        p++;
                    }
                }
                Progress.sendIncrementProgress(progressHandler, 1);
            }
            painted.setPixels(newPixels, 0, wp, 0, 0, wp, hp);
        }

        // Calculate the proportions of the result and create it. The result
        // has more pixels than the bitmap we paint, it has the maximum
        // number of pixels possible with the original outline (while
        // maintaining the same aspect ratio as the drawing).
        final float aspectRatio = (float) painted.getWidth() / painted.getHeight();
        int hr = originalOutlineBitmap.getHeight();
        int wr = (int) (hr * aspectRatio);
        if (wr > originalOutlineBitmap.getWidth())
        {
            wr = originalOutlineBitmap.getWidth();
            hr = (int) (wr / aspectRatio);
        }
        int nr = wr * hr;
        Bitmap result = Bitmap.createBitmap(wr, hr, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Draw and scale the painted bitmap onto the result.
        canvas.drawBitmap(painted, new Rect(0, 0, painted.getWidth(),
                painted.getHeight()), new Rect(0, 0, wr, hr), paint);
        Progress.sendIncrementProgress(progressHandler, PROGRESS_DRAW_PAINTED);

        // Process the outline, i.e. get which pixels are transparent and which
        // ones are not. This is almost the same as what we do when loading
        // except that this image has more pixels.
        Bitmap cropped = Bitmap.createBitmap(wr, hr, Bitmap.Config.ARGB_8888);
        {
            int[] pixels = new int[nr];
            // While getting the pixels, we also crop the unneeded parts.
            originalOutlineBitmap.getPixels(pixels, 0, wr,
                    (originalOutlineBitmap.getWidth() - wr) / 2,
                    (originalOutlineBitmap.getHeight() - hr) / 2, wr, hr);
            for (int i2 = 0; i2 < PROGRESS_SCAN_OUTLINE; i2++)
            {
                final int iStart = i2 * nr / PROGRESS_SCAN_OUTLINE;
                final int iEnd = (i2 + 1) * nr / PROGRESS_SCAN_OUTLINE;
                for (int i = iStart; i < iEnd; i++)
                {
                    int alpha = 255 - DrawUtils.brightness(pixels[i]);
                    pixels[i] = alpha << 24;
                }
                Progress.sendIncrementProgress(progressHandler, 1);
            }
            cropped.setPixels(pixels, 0, wr, 0, 0, wr, hr);
        }

        // As a final drawing step, draw the outline onto the result.
        canvas.drawBitmap(cropped, 0, 0, paint);
        Progress.sendIncrementProgress(progressHandler, PROGRESS_DRAW_OUTLINE);

        try
        {
            // Write the result to the dest file.
            file.getParentFile().mkdirs();
            OutputStream outStream = new FileOutputStream(file);
            result.compress(Bitmap.CompressFormat.PNG, 90, outStream);
            outStream.close();
            Progress.sendIncrementProgress(progressHandler, PROGRESS_SAVE);
        }
        catch (IOException e)
        {
            progressHandler.sendEmptyMessage(Progress.MESSAGE_DONE_ERROR);
            return;
        }

        progressHandler.sendEmptyMessage(Progress.MESSAGE_DONE_OK);
    }

    public synchronized boolean isInitialized()
    {
        return _state._paintedBitmap != null;
    }

    public synchronized void setPaintColor(int color)
    {
        _state._color = color;
        _paint.setColor(color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        synchronized (this)
        {
            if (_state._width == 0 || _state._height == 0)
            {
                _state._width = w;
                _state._height = h;
                if (_lifecycleListener != null)
                {
                    _lifecycleListener.onPreparedToLoad();
                }
            }
        }
    }

    
  
    @Override
    protected synchronized void onDraw(Canvas canvas)
    {
        if (_state._paintedBitmap != null)
        {
            canvas.drawBitmap(_state._paintedBitmap, 0, 0, _paint);
        }
        if (_state._outlineBitmap != null)
        {
            canvas.drawBitmap(_state._outlineBitmap, 0, 0, _paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            paint((int) e.getX(), (int) e.getY());
        }
        return true;
    }

    private synchronized void paint(int x, int y)
    {
        // Copy the original mask to the working mask because it will be
        // modified.
        System.arraycopy(_state._paintMask, 0, _state._workingMask, 0,
                _state._width * _state._height);

        // Do the nasty stuff.
        FloodFill.fillRaw(x, y, _state._width, _state._height, _state._workingMask,
                _state._pixels, _state._color);

        // And now copy all the pixels back.
        _state._paintedBitmap.setPixels(_state._pixels, 0, _state._width, 0, 0,
                _state._width, _state._height);
        invalidate();
    }
    private static final int ALPHA_TRESHOLD = 224;
    // The listener whom we notify when ready to load images.
    private LifecycleListener _lifecycleListener;

    // We keep the state of the current drawing in a different class so that
    // we can quickly save and restore it when an orientation change happens.
    // Members of this class are not allowed to contain any references to the
    // view hierarchy.
    private static class State
    {
        // Bitmap containing the outlines that are never changed.

        private Bitmap _outlineBitmap;
        // Bitmap containing everything we have painted so far.
        private Bitmap _paintedBitmap;
        // Dimensions of both bitmaps.
        private int _height;
        private int _width;
        // Paint with the currently selected color.
        private int _color;
        // paintMask has 0 for each pixel that cannot be modified and 1
        // for each one that can.
        private byte _paintMask[];
        // workingMask is in fact only needed during the fill - it is a copy
        // of paintMask that is modified during the fill. To avoid reallocating
        // it each time we store it as a member.
        private byte _workingMask[];
        // All the pixels in _paintedBitmap. Because accessing an int array is
        // much faster than accessing pixels in a bitmap, we operate on this
        // and use setPixels() on the bitmap to copy them back.
        private int _pixels[];
    }
    private State _state;
    private Paint _paint;
}
