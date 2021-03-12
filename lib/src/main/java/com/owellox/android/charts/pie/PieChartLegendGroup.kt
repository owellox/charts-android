/*
 * Copyright 2021 Owellox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.owellox.android.charts.pie

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children

class PieChartLegendGroup
constructor(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Let's depend on the parent-recommended measurements.
        val pWidth = MeasureSpec.getSize(widthMeasureSpec)
        val pWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val pHeight = MeasureSpec.getSize(heightMeasureSpec)
        val pHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        var childLeft: Int
        var childTop = 0
        var childRight = 0
        var childBottom = 0

        getLegends().forEach { legend ->
            var wSpec = MeasureSpec.makeMeasureSpec(pWidth, MeasureSpec.AT_MOST)
            var hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            measureChild(legend, wSpec, hSpec)
            wSpec = MeasureSpec.makeMeasureSpec(legend.measuredWidth, MeasureSpec.EXACTLY)
            hSpec = MeasureSpec.makeMeasureSpec(legend.measuredHeight, MeasureSpec.EXACTLY)
            measureChild(legend, wSpec, hSpec)

            val childWidth = legend.measuredWidth
            val childHeight = legend.measuredHeight
            childLeft = childRight
            childRight = childLeft + childWidth
            if (childRight >= pWidth) {
                childLeft = 0
                childRight = childLeft + childWidth
                childTop = childBottom
            }
            childBottom = childTop + childHeight
        }
        setMeasuredDimension(pWidth, childBottom)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = measuredWidth
        val height = measuredHeight
        var childLeft: Int
        var childTop = 0
        var childRight = 0
        var childBottom = 0
        getLegends().forEach {
            val childWidth = it.measuredWidth
            val childHeight = it.measuredHeight
            childLeft = childRight
            childRight = childLeft + childWidth
            if (childRight >= width) {
                childLeft = 0
                childRight = childLeft + childWidth
                childTop = childBottom
            }
            childBottom = childTop + childHeight
            it.layout(childLeft, childTop, childRight, childBottom)
        }
    }

    private fun getLegends(): Sequence<PieChartLegend> {
        return children.filterIsInstance<PieChartLegend>()
    }
}