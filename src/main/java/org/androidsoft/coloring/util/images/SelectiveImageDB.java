package org.androidsoft.coloring.util.images;

class SelectiveImageDB implements ImageDB {
    private final ImageDB db;
    private final ImageSelector selector;

    public SelectiveImageDB(ImageDB db, ImageSelector selector) {
        this.db = db;
        this.selector = selector;
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < db.size(); i++) {
            Image image = db.get(i);
            if (selector.isSelected(image)) {
                size++;
            }
        }
        return size;
    }

    @Override
    public Image get(int index) {
        for (int i = 0; i < db.size(); i++) {
            Image image = db.get(i);
            if (selector.isSelected(image)) {
                if (index <= 0) {
                    return image;
                }
                index--;
            }
        }
        return new NullImage();
    }

    @Override
    public void attachObserver(Subject.Observer observer) {
        db.attachObserver(observer);
    }

    interface ImageSelector {
        boolean isSelected(Image image);
    }
}
