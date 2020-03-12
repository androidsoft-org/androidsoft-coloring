package org.androidsoft.coloring.util.images;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.util.Log;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.imports.ImagePreview;

import java.io.ByteArrayOutputStream;

public class BitmapImage implements ImageDB.Image {
    private Bitmap bitmap;

    public BitmapImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void asPreviewImage(ImagePreview preview, LoadImageProgress progress) {
        // create a scaled down version of a bitmap
        // see https://stackoverflow.com/a/4837803
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, preview.getWidth(), preview.getHeight(), false);
        preview.done(bitmap);
    }

    @Override
    public boolean canBePainted() {
        return true;
    }

    @Override
    public void asPaintableImage(ImagePreview preview, LoadImageProgress progress) {
        preview.done(bitmap);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        // sending a bitmap
        // see https://stackoverflow.com/a/11010565
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        parcel.writeInt(byteArray.length);
        parcel.writeByteArray(byteArray);
        Log.d("BitmapImage->toParcel", "size " + byteArray.length / 1024 + "kb");
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            // sending a bitmap
            // see https://stackoverflow.com/a/11010565
            int length = parcel.readInt();
            byte[] byteArray = new byte[length];
            parcel.readByteArray(byteArray);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            return new BitmapImage(bitmap);
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
