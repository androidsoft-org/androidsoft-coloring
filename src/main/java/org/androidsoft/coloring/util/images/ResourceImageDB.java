package org.androidsoft.coloring.util.images;

import android.content.Context;

import org.androidsoft.coloring.ui.activity.ChoosePictureActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.quelltext.coloring.R;

/*
 * This object contains all images from the res/drawable folder
 */
public class ResourceImageDB implements ImageDB {
    private static final String IMAGE_PREFIX = "outline";
    private final List<Image> images = new ArrayList<>();

    public ResourceImageDB() {
        Field[] drawables = R.drawable.class.getDeclaredFields();
        for (int i = 0; i < drawables.length; i++) {
            String name = drawables[i].getName();
            try {
                if (name.startsWith(IMAGE_PREFIX))
                {
                    images.add(new DrawableResourceImage(drawables[i].getInt(null)));
                }
            } catch (IllegalAccessException e) {}
        }

    }

    @Override
    public int size() {
        return images.size();
    }

    @Override
    public Image get(int index) {
        if (index > images.size()) {
            return new NullImage();
        }
        return images.get(index);
    }

    @Override
    public void attachObserver(Subject.Observer observer) {
    }

    public Image randomImage() {
        int index = new Random().nextInt(size());
        return get(index);
    }
}
