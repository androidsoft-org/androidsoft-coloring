package org.androidsoft.coloring.util.cache;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.imports.ImagePreview;

public class NullImageCache implements ImageCache {
    @Override
    public boolean asPreviewImage(ImageDB.Image image, ImagePreview preview, LoadImageProgress progress) {
        image.asPreviewImage(preview, progress);
        return false;
    }
}
