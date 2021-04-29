package org.androidsoft.coloring.util.images;

import java.util.ArrayList;
import java.util.List;

public class JoinedImageDB extends Subject implements ImageDB, Subject.Observer {

    private List<ImageDB> imageDBs = new ArrayList<>();;

    public JoinedImageDB() {
    }

    JoinedImageDB(List<ImageDB> dbs) {
        for (ImageDB db : dbs) {
            add(db);
        }
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

    public void add(ImageDB imageDB) {
        imageDBs.add(imageDB);
        imageDB.attachObserver(this);
    }

    public void add(Image image) {
        add(new SingeImageDB(image));
    }

    @Override
    public void update() {
        notifyObservers();
    }
}
