package org.androidsoft.coloring.util.imports;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface ImagePreview {
    void setImage(Bitmap image);
    int getWidth();
    int getHeight();
    InputStream openInputStream(Uri uri) throws FileNotFoundException;
    void done(Bitmap bitmap);
}
