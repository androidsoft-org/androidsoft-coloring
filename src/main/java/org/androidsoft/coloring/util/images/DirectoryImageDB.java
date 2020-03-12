package org.androidsoft.coloring.util.images;

import android.content.Context;

import org.androidsoft.coloring.util.BitmapSaver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryImageDB implements ImageDB {

    private final File directory;

    public DirectoryImageDB(File directory) {
        this.directory = directory;
    }

    public static ImageDB atSaveLocationOf(Context context) {
        File directory = BitmapSaver.getSavedImagesDirectory(context);
        return new DirectoryImageDB(directory);
    }

    @Override
    public int size() {
        return getFileList().size();
    }

    protected List<File> getFileList() {
        return imagesInDirectory(directory);
    }

    protected List<File> imagesInDirectory(File directory) {
        File[] files = directory.listFiles();
        List<File> images = new ArrayList<>();
        for (File file : files) {
            if (isImage(file)) {
                // add latest files to the front
                images.add(0, file);
            }
        }
        return images;
    }

    protected boolean isImage(File file) {
        return file.getName().toLowerCase().endsWith(".png");
    }

    @Override
    public Image get(int index) {
        List<File> files = getFileList();
        if (index >= files.size()) {
            return new NullImage();
        }
        File file = files.get(index);
        return PreparedUriImage.fromFile(file);
    }

    @Override
    public void attachObserver(Subject.Observer observer) {
    }
}
