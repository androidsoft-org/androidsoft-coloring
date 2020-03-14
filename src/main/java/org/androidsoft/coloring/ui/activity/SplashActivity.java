/* Copyright (c) 2010-2011 Pierre LEVY androidsoft.org
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.androidsoft.coloring.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.androidsoft.utils.ui.WhatsNewActivity;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import eu.quelltext.coloring.R;


/**
 * Splash activity
 * @author Pierre Levy
 */
public class SplashActivity extends WhatsNewActivity
{

    private static final String CHANGELOG_FOLDER = "changelogs";
    private int permissionRequest = 0;
    private Button mButtonPlay;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        setContentView(R.layout.splash);

        OnClickListener closeThis = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, PaintActivity.class);
                startActivity(intent);
            }
        };

        mButtonPlay = findViewById(R.id.button_go);
        mButtonPlay.setOnClickListener(closeThis);

        ImageView image = (ImageView) findViewById(R.id.image_splash);
        image.setImageResource(R.drawable.ic_logo);

        LinearLayout splashScreen = findViewById(R.id.splash_screen);
        splashScreen.setOnClickListener(closeThis);

        TextView versionText = findViewById(R.id.version_text_view);
        // set the version
        // see https://developer.android.com/guide/topics/resources/string-resource#java
        // credits to https://stackoverflow.com/a/51109685/1320237
        versionText.setText(getString(R.string.credits_current_version, getVersionName()));

        checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_read_external_storage);
        checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_write_external_storage);
        checkForPermission(Manifest.permission.EXPAND_STATUS_BAR, R.string.permission_expand_status_bar);
        checkForPermission(Manifest.permission.SYSTEM_ALERT_WINDOW, R.string.permission_expand_status_bar);
    }

    public String getVersionName() {
        // from WhatsNewActivity
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "?";
        }
    }

    public int getVersionCode() {
        // from WhatsNewActivity
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    @Override
    public int getFirstRunDialogTitleRes()
    {
        return R.string.first_run_dialog_title;
    }

    @Override
    public int getFirstRunDialogMsgRes()
    {
        return R.string.first_run_dialog_message;
    }

    @Override
    public int getWhatsNewDialogTitleRes()
    {
        return R.string.whats_new_dialog_title;
    }

    @Override
    public int getWhatsNewDialogMsgRes()
    {
        return R.string.whats_new_dialog_message;
    }

    @Override
    public String getWhatsNewDialogMsgString() {
        // move changelog slightly by tab
        String changelog = getChangelog().replace("\n", "\n\t");
        return getString(getWhatsNewDialogMsgRes(), getVersionName(), changelog);
    }

    // return the changelog or null if none exists
    private String getChangelog() {
        int versionCode = getVersionCode();
        // for language, see https://stackoverflow.com/a/23168383/1320237
        String language = Locale.getDefault().getLanguage(); // "en"
        String country = Locale.getDefault().getCountry(); // "US"
        String[] filenames = new String[]{
                getChangelogFileNameForLanguageAndCountry(language, country),
                getChangelogFileNameForLanguage(language),
                getChangelogFileNameForLanguageAndCountry(Locale.ENGLISH.getLanguage(), Locale.US.getCountry()),
                getChangelogFileNameForLanguage(Locale.ENGLISH.getLanguage()),
        };
        // opening an asset
        // see https://inducesmile.com/android-programming/how-to-read-a-file-from-assets-folder-in-android/
        InputStream in = null;
        AssetManager assets = getAssets();
        for (String filename: filenames) {
            // check if the asset exists, see https://stackoverflow.com/a/38240347/1320237
            try {
                in = assets.open(filename, AssetManager.ACCESS_BUFFER);
                // read all bytes
                // see https://stackoverflow.com/a/859076/1320237
                byte[] content = IOUtils.toByteArray(in);
                return new String(content, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getChangelogFileNameForLanguage(String language) {
        return CHANGELOG_FOLDER + "/" + language + "/" + getVersionCode() + ".txt";
    }

    private String getChangelogFileNameForLanguageAndCountry(String language, String country) {
        return CHANGELOG_FOLDER + "/" + language + "-" + country + "/" + getVersionCode() + ".txt";
    }

    public void checkForPermission(final String permissionName, int explanationResourceId) {
        // check for permissions
        // see https://developer.android.com/training/permissions/requesting#java
        if (ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                // see https://stackoverflow.com/a/2115770
                new AlertDialog.Builder(this)
                        .setMessage(explanationResourceId)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                ActivityCompat.requestPermissions(SplashActivity.this,
                                        new String[]{permissionName},
                                        permissionRequest++);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{permissionName},
                        permissionRequest++);
            }
        } else {
            // Permission has already been granted
        }
    }
}
