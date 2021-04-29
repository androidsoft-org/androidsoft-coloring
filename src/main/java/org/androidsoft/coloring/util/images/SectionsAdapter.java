package org.androidsoft.coloring.util.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.cache.ImageCache;
import org.androidsoft.coloring.util.cache.MemoryImageCache;
import org.androidsoft.coloring.util.cache.NullImageCache;
import org.androidsoft.coloring.util.imports.FixedSizeImagePreview;

import eu.quelltext.coloring.R;

public class SectionsAdapter extends RecyclerView.Adapter {
    private static final int MAX_WIDTH_HEIGHT_MULTIPLIER = 3;
    private final SettingsImageDB imageDB;
    private final int layoutId;
    private final int[] imageViewIds;
    private final int numberOfImagesPerRow;
    private final ImageCache cache = new NullImageCache(); // TODO: use MemoryImageCache if willed so
    private ImageListener imageListener = new NullImageListener();

    public SectionsAdapter(SettingsImageDB imageDB, int layoutId, int[] imageViewIds) {
        this.imageDB = imageDB;
        imageDB.attachObserver(new Subject.Observer() {
            @Override
            public void update() {
                notifyDataSetChanged();
            }
        });
        this.layoutId = layoutId;
        this.imageViewIds = imageViewIds;
        this.numberOfImagesPerRow = imageViewIds.length;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(layoutId, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int row) {
        ImageDB.Image[] images = new ImageDB.Image[numberOfImagesPerRow];
        for (SettingsImageDB.Entry section : imageDB.entries()) {
            int sectionRows = numberOfRows(section);
            if (sectionRows > row) {
                // we found the section!
                int start = row * numberOfImagesPerRow;
                for (int i = 0; i < numberOfImagesPerRow; i++) {
                    images[i] = section.get(start + i);
                }
                // display the images
                ViewHolder holder = (ViewHolder)viewHolder;
                holder.display(images);
                if (row == 0) {
                    holder.addSectionStart(section);
                } else {
                    holder.removeSectionStart();
                }
                break;
            } else {
                row -= sectionRows;
            }
        }
    }

    @Override
    public int getItemCount() {
        int rows = 0;
        for (SettingsImageDB.Entry section : imageDB.entries()) {
            rows += numberOfRows(section);
        }
        return rows;
    }

    private int numberOfRows(SettingsImageDB.Entry section) {
        int numberOfImages = section.size();
        // increase rows according to the images
        int rows = numberOfImages / numberOfImagesPerRow;
        if (numberOfImages % numberOfImagesPerRow != 0) {
            rows++; // does not fully fit the count
        }
        return rows;
    }

    public void setImageListener(ImageListener listener) {
        imageListener = listener;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private final View root;
        private final TextView title;
        private final TextView description;
        private final View titleContainer;


        public ViewHolder(@NonNull View root) {
            super(root);
            this.root = root;
            title  = root.findViewById(R.id.title);
            description  = root.findViewById(R.id.description);
            titleContainer  = root.findViewById(R.id.title_container);
        }

        public void display(ImageDB.Image[] images) {
            for (int i = 0; i < images.length; i++) {
                ImageView imageView = root.findViewById(imageViewIds[i]);
                final ImageDB.Image image = images[i];
                if (image.canBePainted()) {
                    int width = getWidthOf(imageView);
                    if (!cache.asPreviewImage(image, new ThumbPreview(imageView, width), new LoadImageProgress(null, null))) {
                        imageView.setImageResource(R.drawable.download);
                    }
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imageListener.onImageChosen(image);
                        }
                    });
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        }

        private int getWidthOf(ImageView imageView) {
            int width = imageView.getWidth();
            int maxWidth = getScreenWidth() / numberOfImagesPerRow;
            width = width == 0 ? maxWidth : width;
            if (width > maxWidth) {
                width = maxWidth;
            }
            return width;
        }

        private int getScreenWidth() {
            // from https://stackoverflow.com/a/4744499
            Context context = root.getContext();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            try {
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            } catch (ClassCastException e) {
                e.printStackTrace();
                return 400; // default width
            }
            return displayMetrics.widthPixels;
        }

        public void addSectionStart(SettingsImageDB.Entry section) {
            titleContainer.setVisibility(View.VISIBLE);
            title.setText(section.getName());
            int numberOfImages = section.size();
            if (numberOfImages >= 5) {
                String description = this.description.getContext().getString(R.string.image_list_section_description, numberOfImages);
                this.description.setText(description);
                this.description.setVisibility(View.VISIBLE);
            } else {
                this.description.setVisibility(View.GONE);
            }
        }

        public void removeSectionStart() {
            titleContainer.setVisibility(View.GONE);
        }
    }

    class ThumbPreview extends FixedSizeImagePreview {

        private final ImageView imageView;

        public ThumbPreview(ImageView imageView, int maxWidth) {
            super(imageView.getContext(), maxWidth, maxWidth * MAX_WIDTH_HEIGHT_MULTIPLIER);
            this.imageView = imageView;
        }

        @Override
        public void setImage(final Bitmap image) {
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(image);
                }
            });
        }

        @Override
        public void done(Bitmap bitmap) {
            setImage(bitmap);
        }
    }
}
