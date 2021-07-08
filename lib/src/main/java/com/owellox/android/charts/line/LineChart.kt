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

package com.owellox.android.charts.line

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class LineChart
constructor(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var data: LineChartData = LineChartData.empty()
        set(value) {
            field = value
            invalidate()
        }

    private var mChartRect = Rect()
    private var mChartBitmap: Bitmap? = null
    private var mChartCanvas: Canvas? = null
    private val mLineMatrix = Matrix()

    private val defaultLineThickness = context.resources.displayMetrics.density * 2

    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
        strokeWidth = defaultLineThickness
    }
    var lineThickness = defaultLineThickness
        set(value) {
            field = value
            mLinePaint.strokeWidth = value
            invalidate()
        }

    init {
        if (isInEditMode) {
            data = LineChartData.editModeData()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hPaddingTotal = paddingStart + paddingEnd
        val vPaddingTotal = paddingTop + paddingBottom
        val minWidth = max(suggestedMinimumWidth, hPaddingTotal)
        val minHeight = max(suggestedMinimumHeight, vPaddingTotal)

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        var w = MeasureSpec.getSize(widthMeasureSpec)
        w = when (wMode) {
            MeasureSpec.AT_MOST -> {
                min(minWidth, w)
            }
            else -> w
        }

        var h = MeasureSpec.getSize(heightMeasureSpec)
        h = when (hMode) {
            MeasureSpec.AT_MOST -> {
                min(minHeight, h)
            }
            else -> h
        }

        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mChartCanvas = null
        mChartBitmap?.recycle()
        mChartBitmap = null
        try {
            val wPaddingTotal = paddingLeft + paddingRight
            val hPaddingTotal = paddingTop + paddingBottom
            val width = max(0, w - wPaddingTotal)
            val height = max(0, h - hPaddingTotal)
            val wEnough = width >= wPaddingTotal
            val hEnough = height >= hPaddingTotal
            mChartRect.set(
                // Left
                if (wEnough) paddingLeft else w / 2,
                // Top
                if (hEnough) paddingTop else h / 2,
                // Right
                if (wEnough) w - paddingRight else w / 2,
                // Bottom
                if (hEnough) h - paddingBottom else h / 2
            )
            mChartBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mChartCanvas = mChartBitmap?.let { Canvas(it) }
        } catch (e: IllegalArgumentException) {
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mChartCanvas?.also { drawChart(it) }
        mChartBitmap?.also {
            canvas.drawBitmap(it, null, mChartRect, null)
        }
    }

    private fun drawChart(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val data = data
        data.apply {
            lines.forEach {
                drawLine(canvas, this, it)
            }
        }
    }

    private fun drawLine(canvas: Canvas, data: LineChartData, line: LineChartLine) {
        val points = line.points
        mLinePaint.color = line.color
        // Chart drawing range.
        val chartWidth = mChartRect.width().toFloat()
        val chartHeight = mChartRect.height().toFloat()

        val xLength = abs(data.spec.maxX - data.spec.minX)
        val yLength = abs(data.spec.maxY - data.spec.minY)

        val xChartMid = chartWidth / 2f
        val yChartMid = chartHeight / 2f

        val xChartScale = (chartWidth - lineThickness) / chartWidth
        val yChartScale = (chartHeight - lineThickness) / chartHeight

        val xMin = data.spec.minX
        val yMin = data.spec.minY

        points.forEachIndexed { i, p1 ->
            val p2 = points.getOrNull(i + 1)
            if (p2 != null) {
                val values = floatArrayOf(
                    p1.x, p1.y,
                    p2.x, p2.y
                )
                // 1. Flip the line in y-axis.
                // 2. Scale it up to the chart size.
                mLineMatrix.reset()
                mLineMatrix.preTranslate(-xMin, -yMin)
                mLineMatrix.setScale(1f / xLength, 1f / yLength)
                mLineMatrix.postScale(chartWidth, chartHeight)
                mLineMatrix.mapPoints(values)

                mLineMatrix.reset()
                mLineMatrix.setScale(1f, -1f, xChartMid, yChartMid)
                mLineMatrix.postScale(xChartScale, yChartScale, xChartMid, yChartMid)
                mLineMatrix.mapPoints(values)

                canvas.drawLines(values, mLinePaint)
            }
        }
    }
}