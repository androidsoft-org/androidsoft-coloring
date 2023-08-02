package org.androidsoft.coloring.util.images;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Parcel;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.imports.BlackAndWhiteImageImport;
import org.androidsoft.coloring.util.imports.ImagePreview;
import org.androidsoft.coloring.util.imports.UriImageImport;

import java.io.File;

/* This is an image which is prepared (black and white) to drawing located at a Uri.
 *
 */
public class PreparedUriImage implements ImageDB.Image {
    private final Uri uri;
    private final boolean isBlackAndWhite;

    public static PreparedUriImage fromResourceId(Context context, int resourceId) {
        // get Uri, see https://stackoverflow.com/a/19567921/1320237
        Resources resources = context.getResources();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resourceId) + '/' + resources.getResourceTypeName(resourceId) + '/' + resources.getResourceEntryName(resourceId));
        return new PreparedUriImage(uri, false);
    }

    public PreparedUriImage(Uri uri, boolean isBlackAndWhite) {
        this.uri = uri;
        this.isBlackAndWhite = isBlackAndWhite;
    }

    public static ImageDB.Image fromPaintedImageFile(File file) {
        return new PreparedUriImage(Uri.fromFile(file), true);
    }

    @Override
    public void asPreviewImage(ImagePreview preview, LoadImageProgress progress) {
        // todo: speed up by caching
        new UriImageImport(uri, progress, preview).start();
    }

    @Override
    public boolean canBePainted() {
        return true;
    }

    @Override
    public void asPaintableImage(ImagePreview preview, LoadImageProgress progress) {
        if (isBlackAndWhite) {
            new UriImageImport(uri, progress, preview).start();
        } else {
            new BlackAndWhiteImageImport(uri, progress, preview).start();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(uri.toString());
        // java.lang.NoSuchMethodError: No virtual method writeBoolean
        parcel.writeInt(isBlackAndWhite ? 1 : 0);
    }

    public static Creator CREATOR = new Creator() {

        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            Uri uri = Uri.parse(parcel.readString());
            boolean isBlackAndWhite = parcel.readInt() != 0;
            return new PreparedUriImage(uri, isBlackAndWhite);
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
