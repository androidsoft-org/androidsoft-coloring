package org.androidsoft.coloring.ui.widget;

import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import eu.quelltext.coloring.R;

public class LoadImageProgress {

    private final ProgressBar progressBar;
    private long lastStep;
    private TextView textView;
    private final Handler handler;

    public LoadImageProgress(ProgressBar progressBar, TextView textView) {
        this.progressBar = progressBar;
        this.textView = textView;
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMax(STEPS);
        }
        handler = new Handler();
        lastStep = System.nanoTime();
        stepStart();

    }

    private void step(final int step, final int textId) {
        // stepping should be thread save
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setProgress(step);
                }
                if (textView != null) {
                    textView.setText(textId);
                }
            }
        });
        long newStep = System.nanoTime();
        Log.d("progress", "step " + step + ": " + (newStep - lastStep) / 1000000 + "ms");
        lastStep = newStep;
    }

    public void stepStart() {
        step(0, R.string.progress_starting);
    }

    public void stepDone() {
        step(STEPS, R.string.progress_done);
    }

    public void stepFail() {
        step(STEPS, R.string.progress_error);
    }

    public void stepInputPreview() {
        step(1, R.string.progress_input_preview);
    }

    public void stepPreparingClustering() {
        step(2, R.string.progress_preparing_clustering);
    }

    public void stepSampleDataForClassification() {
        step(3, R.string.progress_sample_data);
    }

    public void stepClusteringData() {
        step(4, R.string.progress_clustering_data);
    }

    public void stepCreateClusterImage() {
        step(5, R.string.progress_create_cluster);
    }

    public void stepShowClusterImage() {
        step(6, R.string.progress_show_cluster);
    }

    public void stepRemovingNoise() {
        step(7, R.string.progress_removing_noise);
    }

    public void stepShowSmoothedImage() {
        step(8, R.string.progress_show_smoothed_image);
    }

    public void stepConnectingComponents() {
        step(9, R.string.progress_connecting_components);
    }

    public void stepMeasuringAreas() {
        step(10, R.string.progress_measuring_areas);
    }

    public void stepShowComponents() {
            step(11, R.string.progress_show_components);
    }

    public void stepDrawLinesAround() {
        step(12, R.string.progress_draw_lines);
    }

    private static final int STEPS = 15; // do not forget to change when you add steps

    public void stepConvertingToBinaryImage() {
        step(4, R.string.progress_convert_binary);
    }
}
