package org.androidsoft.coloring.util;

import org.androidsoft.coloring.util.images.GalleryImageDB;

public class DefaultGalleryImageDB extends GalleryImageDB {
    private final int description;

    public DefaultGalleryImageDB(String url, int description) {
        super(url);
        this.description = description;
    }

    public int getDescriptionResourceId() {
        return description;
    }

}
