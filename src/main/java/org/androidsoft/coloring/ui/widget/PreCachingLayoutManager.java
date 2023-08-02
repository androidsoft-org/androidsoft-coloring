package org.androidsoft.coloring.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/* This layout manager loads images ahead.
 * see https://developer.android.com/reference/android/support/v7/widget/LinearLayoutManager#setsmoothscrollbarenabled
 * thanks to https://developer.android.com/reference/android/support/v7/widget/LinearLayoutManager#getextralayoutspace
 * thanks to https://github.com/ovy9086/recyclerview-playground/blob/master/app/src/main/java/com/olu/recyclerview/widget/PreCachingLayoutManager.java
 * thanks to https://androiddevx.wordpress.com/2014/12/05/recycler-view-pre-cache-views/
 * thanks to https://github.com/facebook/fresco/issues/335#issuecomment-110280822
 * also https://github.com/ovy9086/recyclerview-playground/blob/master/app/src/main/java/com/olu/recyclerview/fragments/CatsListFragment.java#L45
 * deprecated, see https://developer.android.com/reference/androidx/recyclerview/widget/LinearLayoutManager?hl=en#getExtraLayoutSpace(androidx.recyclerview.widget.RecyclerView.State)
 * see https://developer.android.com/reference/androidx/recyclerview/widget/LinearLayoutManager?hl=en#calculateExtraLayoutSpace(androidx.recyclerview.widget.RecyclerView.State,%20int%5B%5D)
 */
public class PreCachingLayoutManager extends LinearLayoutManager {
    private final int extraLayoutSpace;

    public PreCachingLayoutManager(Context context, int extraLayoutSpace) {
        super(context);
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected void calculateExtraLayoutSpace(@NonNull RecyclerView.State state, @NonNull int[] extraLayoutSpace) {
        extraLayoutSpace[0] = this.extraLayoutSpace;
        extraLayoutSpace[1] = this.extraLayoutSpace;
    }
}
