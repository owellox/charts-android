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

package com.owellox.android.charts.donut

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.owellox.android.charts.R
import com.owellox.android.charts.pie.PieChart
import kotlin.math.min

class DonutChart
constructor(context: Context, attrs: AttributeSet) : PieChart(context, attrs) {
    private var mSectorTrackThickness: Float = 0f
        set(value) {
            field = value
            measureTrackThickness()
        }

    private val mCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    private val mCenterRect = RectF()

    init {
        loadStyledAttributes(attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureTrackThickness()
    }

    override fun onDrawChart(canvas: Canvas) {
        super.onDrawChart(canvas)
        canvas.drawOval(mCenterRect, mCenterPaint)
    }

    private fun measureTrackThickness() {
        val diameter = diameter
        val closerToZero = min(radius.toFloat(), mSectorTrackThickness)
        val awayFromZero = min(radius.toFloat(), mSectorTrackThickness)
        val right = diameter - awayFromZero
        val bottom = diameter - awayFromZero
        mCenterRect.set(closerToZero, closerToZero, right, bottom)
    }

    private fun loadStyledAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.DonutChart,
            0, 0
        ).apply {
            mSectorTrackThickness = getDimension(
                R.styleable.DonutChart_sectorTrackThickness,
                12f * resources.displayMetrics.density
            )
        }
    }
}