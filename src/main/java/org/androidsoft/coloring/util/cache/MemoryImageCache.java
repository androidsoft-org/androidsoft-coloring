package org.androidsoft.coloring.util.cache;

import android.graphics.Bitmap;
import android.net.Uri;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.imports.ImagePreview;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class MemoryImageCache implements ImageCache {

    HashMap<ImageDB.Image, Bitmap> cache = new HashMap<>();

    @Override
    public boolean asPreviewImage(ImageDB.Image image, ImagePreview preview, LoadImageProgress progress) {
        if (cache.containsKey(image)) {
            progress.stepDone();
            preview.done(cache.get(image));
            return true;
        } else {
            image.asPreviewImage(new CachingPreview(image, preview), progress);
            return false;
        }
    }


    private class CachingPreview implements ImagePreview {
        private final ImageDB.Image image;
        private final ImagePreview preview;

        public CachingPreview(ImageDB.Image image, ImagePreview preview) {
            this.image = image;
            this.preview = preview;
        }

        @Override
        public void setImage(Bitmap image) {
            preview.setImage(image);
        }

        @Override
        public int getWidth() {
            return preview.getWidth();
        }

        @Override
        public int getHeight() {
            return preview.getHeight();
        }

        @Override
        public InputStream openInputStream(Uri uri) throws FileNotFoundException {
            return preview.openInputStream(uri);
        }

        @Override
        public void done(Bitmap bitmap) {
            cache.put(image, bitmap);
            preview.done(bitmap);
        }
    }
}
