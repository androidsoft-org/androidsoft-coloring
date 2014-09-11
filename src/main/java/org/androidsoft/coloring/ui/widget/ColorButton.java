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
package org.androidsoft.coloring.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import org.androidsoft.coloring.R;

public class ColorButton extends ColoringButton
{

    public static final int PADDING_NORMAL_PERCENT = 20;
    public static final int PADDING_PUSHED_PERCENT = 10;
    public static final int PADDING_SELECTED_PERCENT = 12;
    public static final int INSET_HIGHLIGHT_PERCENT = 5;

    public ColorButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColorButton, defStyle, 0);
        _color = a.getColor(R.styleable.ColorButton_color, Color.RED);
        a.recycle();

        _highlightDrawable = new GradientDrawable(Orientation.TOP_BOTTOM,
                new int[]
                {
                    Color.WHITE, Color.TRANSPARENT, Color.TRANSPARENT
                });
        _highlightDrawable.setShape(GradientDrawable.OVAL);
        _colorDrawable = new GradientDrawable();
        _colorDrawable.setColor(_color);
        _colorDrawable.setShape(GradientDrawable.OVAL);
    }

    public ColorButton(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.colorButtonStyle);
    }

    public ColorButton(Context context)
    {
        this(context, null);
    }

    @Override
    public void setSelected(boolean selected)
    {
        if (_selected != selected)
        {
            _selected = selected;
            invalidate();
        }
    }

    public int getColor()
    {
        return _color;
    }

    public void setColor(int color)
    {
        if (color != _color)
        {
            _color = color;
            _colorDrawable.setColor(_color);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        boolean pushedDown = isPushedDown();

        final int w = getWidth();
        final int h = getHeight();
        final int minwh = Math.min(w, h);

        int i = minwh * INSET_HIGHLIGHT_PERCENT / 100;
        int p = 0;
        if (pushedDown)
        {
            p = minwh * PADDING_PUSHED_PERCENT / 100;
        }
        else
        {
            if (_selected)
            {
                p = minwh * PADDING_SELECTED_PERCENT / 100;
            }
            else
            {
                p = minwh * PADDING_NORMAL_PERCENT / 100;
            }
        }

        _colorDrawable.setBounds(p, p, w - p, h - p);
        _colorDrawable.draw(canvas);
        _highlightDrawable.setBounds(p + i, p + i, w - p - i, h - p - i);
        _highlightDrawable.draw(canvas);
    }
    private boolean _selected;
    private int _color;
    private GradientDrawable _colorDrawable;
    private GradientDrawable _highlightDrawable;
}
