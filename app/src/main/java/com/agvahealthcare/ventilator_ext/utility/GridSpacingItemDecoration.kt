package com.agvahealthcare.ventilator_ext.utility

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class GridSpacingItemDecoration(private val mSpanCount: Int, itemSize: Int) :
    ItemDecoration() {
    private val mItemSize: Float = itemSize.toFloat()
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildLayoutPosition(view)
        val column = position % mSpanCount
        val parentWidth = parent.width
        val spacing = (parentWidth - mItemSize * mSpanCount).toInt() / (mSpanCount + 1)
        outRect.left = spacing - column * spacing / mSpanCount
        outRect.right = (column + 1) * spacing / mSpanCount
        if (position < mSpanCount) {
            outRect.top = 25
        }
        outRect.bottom = 12
    }

}