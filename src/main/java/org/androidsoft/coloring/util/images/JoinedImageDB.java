package org.androidsoft.coloring.util.images;

import java.util.ArrayList;
import java.util.List;

public class JoinedImageDB implements ImageDB {

    private List<ImageDB> imageDBs;

    public JoinedImageDB() {
        imageDBs = new ArrayList<>();
    }

    JoinedImageDB(List<ImageDB> dbs) {
        imageDBs = dbs;
    }

    @Override
    public int size() {
        int size = 0;
        for (ImageDB imageDB : imageDBs) {
            size += imageDB.size();
        }
        return size;
    }

    @Override
    public Image get(int index) {
        for (ImageDB imageDB : imageDBs) {
            if (index < imageDB.size()) {
                return imageDB.get(index);
            }
            index -= imageDB.size();
        }
        return new NullImage();
    }

    @Override
    public void attachObserver(Subject.Observer observer) {
        for (ImageDB imageDB : imageDBs) {
            imageDB.attachObserver(observer);
        }
    }

    public void add(ImageDB imageDB) {
        imageDBs.add(imageDB);
    }

    public void add(Image image) {
        imageDBs.add(new SingeImageDB(image));
    }
}
