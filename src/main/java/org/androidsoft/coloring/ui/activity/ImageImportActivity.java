package org.androidsoft.coloring.ui.activity;


import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.androidsoft.coloring.R;
import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.ImageProcessing;
import org.androidsoft.utils.ui.NoTitleActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/* Activity to receive shared images and pass them to the paint activity
 * see https://developer.android.com/training/sharing/receive
 */
public class ImageImportActivity extends NoTitleActivity {

    private ImageView imageView;
    private LoadImageProgress progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_import);

        imageView = findViewById(R.id.imageView);
        progress = new LoadImageProgress(
                (ProgressBar)findViewById(R.id.progressBar),
                (TextView)findViewById(R.id.progress_text));

        // Get intent, action and MIME type
        // see https://developer.android.com/training/sharing/receive
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        Runnable imageProcessing = new Failure();
        Uri imageUri = null;

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (imageUris != null && imageUris.size() >= 1) {
                imageUri = imageUris.get(0);
            }
        }
        if (imageUri != null) {
            imageProcessing = new ImageProcessing(imageUri, progress, new ViewImagePreview());
        }

        Thread processor = new Thread(imageProcessing);
        processor.start();
    }

    private class Failure implements Runnable {

        @Override
        public void run() {
            progress.stepFail();
        }
    }

    /* This class cares for the images being shown to the user */
    private class ViewImagePreview implements ImageProcessing.ImagePreview {
        private final Handler handler;
        private final int width;
        private final int height;
        private final ContentResolver contentResolver;

        public ViewImagePreview() {
            handler = new Handler();
            width = imageView.getWidth();
            height = imageView.getHeight();
            contentResolver = ImageImportActivity.this.getContentResolver();
        }

        @Override
        public void setImage(final Bitmap image) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(image);
                    if ((image.getHeight() < image.getWidth()) != (imageView.getHeight() < imageView.getWidth())) {
                        imageView.setRotation(90);
                    }
                }
            });
        }

        @Override
        public double getWidth() {
            return width;
        }

        @Override
        public double getHeight() {
            return height;
        }

        @Override
        public InputStream openInputStream(Uri uri) throws FileNotFoundException {
            return contentResolver.openInputStream(uri);
        }
    }
}
