package org.androidsoft.coloring.util;

import org.androidsoft.coloring.ui.activity.ChoosePictureActivity;
import org.androidsoft.coloring.util.images.ImageDB;
import org.androidsoft.coloring.util.images.JoinedImageDB;

public class Settings {
    private static final String[] GALLERY_URLS = new String[]{
            "https://gallery.quelltext.eu"
    };

    public static Settings of(ChoosePictureActivity choosePictureActivity) {
        return new Settings();
    }

    public ImageDB getGalleryImageDB() {
        return new JoinedImageDB();
    }
}
