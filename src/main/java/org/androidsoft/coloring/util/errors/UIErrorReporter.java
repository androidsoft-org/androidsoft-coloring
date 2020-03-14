package org.androidsoft.coloring.util.errors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

import eu.quelltext.coloring.R;

public class UIErrorReporter implements ErrorReporter {
    private final Context context;

    public UIErrorReporter(Context context) {

        this.context = context;
    }

    public static UIErrorReporter of(Context context) {
        return new UIErrorReporter(context);
    }

    @Override
    public void report(final Exception e) {
        e.printStackTrace();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String message = context.getResources().getString(R.string.error_occurred_toast);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                String report = getReport(e);
                copyToClipboard(report);
            }
        });
    }

    private String getReport(Exception e) {
        String email = context.getResources().getString(R.string.error_email);
        String link = context.getResources().getString(R.string.error_link);
        String message = context.getResources().getString(R.string.error_occurred_message, email, link);
        String report = message + "\n\n" + errorToString(e);
        return report;
    }

    private String errorToString(Exception e) {
        // convert an error to a string, see https://stackoverflow.com/a/4812589/1320237
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String appName = context.getResources().getString(R.string.app_name);
        String title = context.getResources().getString(R.string.error_report_title, appName);
        android.content.ClipData clip = android.content.ClipData.newPlainText(title, text);
        clipboard.setPrimaryClip(clip);
    }
}
