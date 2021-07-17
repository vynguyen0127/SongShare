package com.example.songshare;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class OverlapDecoration extends RecyclerView.ItemDecoration {

    //Following code from : http://stackoverflow.com/questions/27633454/how-to-overlap-items-in-linearlayoutmanager-recyclerview-like-stacking-cards
    private final static int overlap = -810;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == 0) {
            outRect.set(0, 0, 0, 0);
        } else {
            outRect.set(0, overlap, 0, 0);
        }
    }

};

