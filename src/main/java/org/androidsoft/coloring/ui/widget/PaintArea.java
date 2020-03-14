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
import org.androidsoft.coloring.util.images.BitmapImage;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.images.NullImage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PaintArea {

    private final ViewGroup.LayoutParams layoutParams;
    private Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
    private int paintColor;
    private final ImageView view;

    public PaintArea(ImageView view) {
        this.view = view;
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return onTouchEvent(motionEvent);
            }
        });
        layoutParams = view.getLayoutParams();
    }


    public boolean canPaint() {
        return bitmap != null;
    }

    public ImageDB.Image getImage() {
        if (canPaint()) {
            return new BitmapImage(bitmap);
        }
        return new NullImage();
    }

    public void setImageBitmap(final Bitmap bm) {
        setImageBitmapWithSameSize(bm);
        view.setLayoutParams(layoutParams);
        view.setRotation(0);
        // use post to defer execution until the size of the view is determined
        view.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                int bmHeight = bm.getHeight();
                int bmWidth = bm.getWidth();
                int maxWidth = ((View)view.getParent()).getWidth();
                int maxHeight = ((View)view.getParent()).getHeight();
                if ((bmHeight < bmWidth) != (maxHeight < maxWidth)) {
                    // the image would best be rotated
                    // scale it so it fits the maximum bounds
                    view.setRotation(-90); // bottom to bottom
                    float scale1 = maxHeight / (float)bmWidth;
                    float scale2 = maxWidth / (float)bmHeight;
                    float scale;
                    if (scale1 < scale2) {
                        // height determines size
                        // test with image which is longer than wide but does not fit in maxWidth
                        // example: http://gallery.quelltext.eu/images/freesvg.org/beachview.png
                        layoutParams.width = maxHeight;
                        layoutParams.height = maxHeight * bmHeight / bmWidth;
                    } else {
                        // width determines size
                        // test with image which is longer than wide but does fits in maxWidth
                        // example: http://gallery.quelltext.eu/images/freesvg.org/mascarin-parrot.png
                        // at the end of scaling this, the height of the view should equal maxWidth
                        layoutParams.width = maxWidth * bmWidth / bmHeight;
                        layoutParams.height = maxWidth;
                    }
                } else {
                    float scale1 = maxHeight / (float)bmHeight;
                    float scale2 = maxWidth / (float)bmWidth;
                    if (scale1 < scale2) {
                        // height determines size
                        // test this is the case with the default image from the app
                        layoutParams.width = maxHeight * bmWidth / bmHeight;
                        layoutParams.height = maxHeight;
                    } else {
                        // width determines size
                        // test with an image which is very wide
                        // example: http://gallery.quelltext.eu/images/freesvg.org/cartoon_kids.png
                        // set width and height of view
                        // see https://stackoverflow.com/a/17066696/1320237
                        // see https://stackoverflow.com/a/5042326/1320237
                        layoutParams.width = maxWidth;
                        layoutParams.height = maxWidth * bmHeight / bmWidth;
                    }
                }
                view.setLayoutParams(params);
            }
        });
    }

    private void setImageBitmapWithSameSize(Bitmap bitmap) {
        view.setImageBitmap(bitmap);
        this.bitmap = bitmap;
    }

    public void setPaintColor(int color)
    {
        paintColor = color;
        if (paintColor == FloodFill.BORDER_COLOR) {
            paintColor ++;
        }
    }

    public boolean onTouchEvent(MotionEvent e)
    {
        if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            // play default click sound
            // see https://stackoverflow.com/a/10987791/1320237
            view.playSoundEffect(SoundEffectConstants.CLICK);
            // get the correct position with rotation
            float eventX = e.getX();
            float eventY = e.getY();
            // set the position
            int x = (int)(eventX * bitmap.getWidth() / view.getWidth());
            int y = (int)(eventY * bitmap.getHeight() / view.getHeight());
            Log.d("touch", "(" + e.getRawX() + ") " + eventX + " -> " + x);
            Log.d("touch", "(" + e.getRawY() + ") " + eventY + " -> " + y);
            Bitmap newBitmap = FloodFill.fill(bitmap, x, y, paintColor);
            setImageBitmapWithSameSize(newBitmap);
        }
        return true;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getPaintColor() {
        return paintColor;
    }

    public int getWidth() {
        return view.getWidth() == 0 ? view.getWidth() : 640;
    }

    public int getHeight() {
        return view.getHeight() == 0 ? view.getHeight() : 480;
    }
}
