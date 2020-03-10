package org.androidsoft.coloring.util;

import android.graphics.Bitmap;
import android.net.Uri;

import org.androidsoft.coloring.ui.activity.ImageImportActivity;
import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.images.FileImage;

import java.io.File;

public class CopyImageToLibrary extends ImageProcessing{

    public CopyImageToLibrary(Uri imageUri, LoadImageProgress progress, ImagePreview imagePreview) {
        super(imageUri, progress, imagePreview);
    }

    @Override
    protected void runWithBitmap(Bitmap image) {
        imagePreview.setImage(image);
        progress.stepConvertingToBinaryImage();
        Bitmap binaryImage = FloodFill.asBlackAndWhite(image);
        imagePreview.setImage(binaryImage);
        progress.stepDone();
        imagePreview.done(binaryImage);
    }
}
