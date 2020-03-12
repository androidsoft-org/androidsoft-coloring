package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.imports.ImagePreview;

public interface ImageDB {
    int size();
    Image get(int index);
    void attachObserver(Subject.Observer observer);

    interface Image extends Parcelable {
        // a scaled down version is passed to preview
        void asPreviewImage(ImagePreview preview, LoadImageProgress progress);
        boolean canBePainted();
        // a black and white version is passed to preview
        void asPaintableImage(ImagePreview preview, LoadImageProgress progress);
    }
}
