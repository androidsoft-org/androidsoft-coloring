package org.androidsoft.coloring.util;

import android.graphics.Bitmap;

/* This class is a base class to wrap eu.quelltext.images pixel arrays with bitmaps.
 * create(bitmap) --> getPixelsOfBitmap() --> convert --> getPixelsForNewBitmap() --> getNewBitmap()
 */
public class BitmapConverter {

    private final int width;
    private final int height;
    private Bitmap bitmap;
    private int[] pixels;

    public BitmapConverter(Bitmap bitmap) {
        // fill a pixel in a bitmap https://stackoverflow.com/a/5916506
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        this.bitmap = bitmap;
    }

    public int[] getPixelsOfBitmap() {
        if (pixels != null) {
            return pixels;
        }
        pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        bitmap = null; // delete the bitmap when we do not need it
        return pixels;
    }

    public int[] getPixelsForNewBitmap() {
        return getPixelsOfBitmap();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Bitmap getNewBitmap() {
        // create a new bitmap
        // see https://stackoverflow.com/a/10180908
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(getPixelsForNewBitmap(), 0, width, 0, 0, width, height);
        return bitmap;
    }
}
