package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;

public interface ImageDB {
    int size();
    Image get(int index);

    interface Image extends Parcelable {
        Bitmap asPreviewImage(Context context, int maxWidth);
        boolean isVisible();
        int getResourceId(); // temporary until the PaintView can load arbitrary bitmaps
    }
}
