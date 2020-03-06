package org.androidsoft.coloring.util.images;

class SingeImageDB implements ImageDB {
    private final Image image;

    public SingeImageDB(Image image) {
        this.image = image;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Image get(int index) {
        if (index == 0) {
            return image;
        }
        return new NullImage();
    }
}
