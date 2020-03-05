package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;

import eu.quelltext.coloring.R;

class NullImage implements ImageDB.Image {
    @Override
    public Bitmap asPreviewImage(Context context, int maxWidth) {
        return null;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public Bitmap getImage(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_logo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            return new NullImage();
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
