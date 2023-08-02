package org.androidsoft.coloring.util.imports;

import android.graphics.Bitmap;
import android.net.Uri;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.FloodFill;

public class BlackAndWhiteImageImport extends UriImageImport {

    public BlackAndWhiteImageImport(Uri imageUri, LoadImageProgress progress, ImagePreview imagePreview) {
        super(imageUri, progress, imagePreview);
    }

    @Override
    protected void runWithBitmap(Bitmap image) {
        imagePreview.setImage(image);
        progress.stepConvertingToBinaryImage();
        Bitmap binaryImage = FloodFill.asBlackAndWhite(image);
        super.runWithBitmap(binaryImage);
    }
}
