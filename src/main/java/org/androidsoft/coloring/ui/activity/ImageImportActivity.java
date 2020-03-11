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

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.ConvertingImageImport;
import org.androidsoft.coloring.util.ImageProcessing;
import org.androidsoft.coloring.util.CopyImageToLibrary;
import org.androidsoft.coloring.util.images.BitmapImage;
import org.androidsoft.utils.ui.NoTitleActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import eu.quelltext.coloring.R;

import static org.androidsoft.coloring.ui.activity.PaintActivity.ARG_IMAGE;

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

        openImageFromIntent(getIntent());
    }

    private void openImageFromIntent(final Intent intent) {
        // start the receiving when the layouting is done so we know the size of what we are showing
        // see https://stackoverflow.com/a/24035591/1320237
        imageView.post(new Runnable() {
            @Override
            public void run() {
                // Get intent, action and MIME type
                // see https://developer.android.com/training/sharing/receive
                String action = intent.getAction();
                String type = intent.getType();
                Uri linkData = intent.getData();

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
                } else if (Intent.ACTION_VIEW.equals(action) && linkData != null) {
                    imageUri = linkData;
                }

                if (imageUri != null) {
                    if (imageUri.toString().toLowerCase().endsWith(".png")) {
                        // import png images directly
                        imageProcessing = new CopyImageToLibrary(imageUri, progress, new ViewImagePreview());
                    } else {
                        imageProcessing = new ConvertingImageImport(imageUri, progress, new ViewImagePreview());
                    }
                }

                Thread processor = new Thread(imageProcessing);
                processor.start();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openImageFromIntent(intent);
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
            return width == 0 ? 640 : width;
        }

        @Override
        public double getHeight() {
            return height == 0 ? 480 :height;
        }

        @Override
        public InputStream openInputStream(Uri uri) throws FileNotFoundException {
            return contentResolver.openInputStream(uri);
        }

        @Override
        public void done(Bitmap bitmap) {
            BitmapImage image = new BitmapImage(bitmap);
            Intent intent = new Intent(ImageImportActivity.this, PaintActivity.class);
            intent.putExtra(ARG_IMAGE, image);
            startActivity(intent);
            finish();
        }
    }
}
