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

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SubscriptionPlan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidsoft.coloring.R;
import org.androidsoft.utils.ui.WhatsNewActivity;

/**
 * Splash activity
 * @author Pierre Levy
 */
public class SplashActivity extends WhatsNewActivity
{

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
        image.setImageResource(R.drawable.splash);

        LinearLayout splashScreen = findViewById(R.id.splash_screen);
        splashScreen.setOnClickListener(closeThis);

        TextView versionText = findViewById(R.id.version_text_view);
        // set the version
        // see https://developer.android.com/guide/topics/resources/string-resource#java
        // credits to https://stackoverflow.com/a/51109685/1320237
        versionText.setText(getString(R.string.credits_current_version, getVersionName()));
    }

    private String getVersionName() {
        // from WhatsNewActivity
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "?";
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
}
