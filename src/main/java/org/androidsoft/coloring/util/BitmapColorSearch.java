package org.androidsoft.coloring.util;

import android.graphics.Bitmap;

import eu.quelltext.images.ColorSearch;

public class BitmapColorSearch extends ColorSearch {
    public BitmapColorSearch(Bitmap bitmap) {
        super(new BitmapConverter(bitmap).getPixelsOfBitmap(), bitmap.getWidth(), bitmap.getHeight());
    }
}
