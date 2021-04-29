package org.androidsoft.coloring.util.cache;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.imports.ImagePreview;

public interface ImageCache {
    boolean asPreviewImage(ImageDB.Image image, ImagePreview thumbPreview, LoadImageProgress progress);
}
