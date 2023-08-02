package org.androidsoft.coloring.util.images;

import android.graphics.Bitmap;

public class BitmapHash {
    public static int hash(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getHeight() * bitmap.getWidth()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int hash = 0;
        for (int i = 0; i < pixels.length; i++) {
            hash += pixels[i];
        }
        return hash & 0xffffffff;
    }
}
