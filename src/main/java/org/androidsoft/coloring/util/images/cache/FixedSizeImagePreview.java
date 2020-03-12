package org.androidsoft.coloring.util.images.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.androidsoft.coloring.util.images.ImagesAdapter;
import org.androidsoft.coloring.util.imports.ImagePreview;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class FixedSizeImagePreview implements ImagePreview {
    private final int maxWidth;
    private final Context context;
    private int maxHeight;

    public FixedSizeImagePreview(Context context, int maxWidth, int maxHeight) {
        this.context = context;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public void setImage(final Bitmap image) {
    }

    @Override
    public int getWidth() {
        return maxWidth;
    }

    @Override
    public int getHeight() {
        return maxHeight;
    }

    @Override
    public InputStream openInputStream(Uri uri) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uri);
    }

    @Override
    public void done(Bitmap bitmap) {
    }
}
