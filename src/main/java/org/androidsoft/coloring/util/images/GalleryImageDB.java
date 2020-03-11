package org.androidsoft.coloring.util.images;

import eu.quelltext.coloring.R;

public class GalleryImageDB implements ImageDB {
    private final String url;

    public GalleryImageDB(String url) {
        this.url = url;
    }

    public int getDescriptionResourceId() {
        return R.string.settings_galleries_user_defined;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Image get(int index) {
        return new NullImage();
    }

    @Override
    public void attach(Subject.Observer observer) {

    }
}
