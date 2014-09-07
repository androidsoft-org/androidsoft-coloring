/*
 * Copyright (C) 2010 Peter Dornbach.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidsoft.coloring.ui.activity;

import org.androidsoft.coloring.ui.widget.ColorButton;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.List;
import org.androidsoft.coloring.R;

public class PickColorActivity extends AbstractColoringActivity implements
        View.OnClickListener
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Apparently this cannot be set from the style.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        setContentView(R.layout.pick_color);

        List<ColorButton> colorButtons = new ArrayList<ColorButton>();
        findAllColorButtons(colorButtons);
        for (int i = 0; i < colorButtons.size(); i++)
        {
            colorButtons.get(i).setOnClickListener(this);
        }

    }

    public void onClick(View view)
    {
        if (view instanceof ColorButton)
        {
            ColorButton button = (ColorButton) view;
            setResult(button.getColor());
            finish();
        }
    }
}
