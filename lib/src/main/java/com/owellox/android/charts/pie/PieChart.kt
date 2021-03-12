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

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnPause
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.owellox.android.charts.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class PieChart
constructor(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        @Suppress("unused")
        private const val TAG = "PieChart"
    }

    private val spec: PieChartSpec = PieChartSpec(context, attrs)
    private val shadeBase: Int = TypedValue().also {
        context.theme.resolveAttribute(R.attr.colorPrimary, it, true)
    }.data

    private val sectorShades = mutableListOf<ColorStateList>()

    private val mSectorRect = RectF()
    private val mSectorPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    private var mSectorGapRect = RectF()
    private val mSectorGapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null

    private var mBitmapTop = 0f
    private var mBitmapLeft = 0f

    protected val diameter: Int
        get() = spec.chartSize
    protected val radius: Int
        get() = diameter / 2

    private var data: PieChartData = PieChartData.empty()
    private var oldData: PieChartData? = null
    private var animatedData: PieChartData? = null

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        val durationMultiplier = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
        duration = (durationMultiplier * 300).toLong()
        interpolator = FastOutSlowInInterpolator()
        addUpdateListener {
            val animator = it!!
            val progress = (animator.animatedValue as Float)
            animateData(progress)
        }
        doOnPause {
            // When cancelled, replace old data with the animated data.
            oldData = animatedData
        }
        doOnEnd {
            // Ensure actual data is shown instead at the end of animation.
            oldData = null
            animatedData = null
            invalidate()
        }
    }

    init {
        if (isInEditMode) {
            data = PieChartData.from(PieChartData.editModeData())
            generateShades()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width: Int
        val height: Int
        val chartSize = spec.chartSize
        if (chartSize > 0) {
            // Not a care in the world.
            width = chartSize
            height = chartSize
        } else {
            // Let's depend on the parent-recommended measurements.
            val pWidth = MeasureSpec.getSize(widthMeasureSpec)
            val pWidthMode = MeasureSpec.getMode(widthMeasureSpec)
            val pHeight = MeasureSpec.getSize(heightMeasureSpec)
            val pHeightMode = MeasureSpec.getMode(heightMeasureSpec)

            val smallest = min(pWidth, pHeight)
            width = if (pWidthMode == MeasureSpec.EXACTLY) {
                pWidth
            } else max(smallest, suggestedMinimumWidth)
            height = if (pHeightMode == MeasureSpec.EXACTLY) {
                pHeight
            } else max(smallest, suggestedMinimumHeight)
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measurePie(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mCanvas?.also { onDrawChart(it) }
        mBitmap?.also {
            canvas.drawBitmap(it, mBitmapLeft, mBitmapTop, null)
        }
    }

    /**
     * Set chart data.
     */
    fun setData(data: PieChartData) {
        oldData = this.data
        this.data = data
        generateShades()
        animator.pause()
        animator.start()
    }

    /**
     * Generate shades if any of the data sectors doesn't have a color assigned.
     */
    private fun generateShades() {
        val needShades = data.sectors.any { it.color == null }
        if (needShades) {
            // Generate shades.
            val count = data.sectors.count()
            for (i in 0..count) {
                val a = ((count - i) / count.toFloat() * 255).toInt()
                val r = Color.red(shadeBase)
                val g = Color.green(shadeBase)
                val b = Color.blue(shadeBase)
                val color = Color.argb(a, r, g, b)
                sectorShades.add(ColorStateList.valueOf(color))
            }
        } else sectorShades.clear()
    }

    /**
     * Invalidate [PieChart] to current progress animation.
     *
     * @param progress Animation progress. Range of 0 - 1.
     */
    private fun animateData(progress: Float) {
        if (progress < 1f) {
            val oldSectors = oldData?.sectors ?: emptyList()
            val animatedSectors = data.sectors.map { sector ->
                val oldValue = oldSectors.find { it.id == sector.id }?.value ?: 0f
                val newValue = sector.value
                val diff = abs(oldValue - newValue)
                val animatedValue =
                    if (newValue > oldValue) oldValue + (diff * progress)
                    else oldValue - (diff * progress)
                sector.copy(value = animatedValue)
            }
            animatedData = PieChartData.from(animatedSectors)
        }
        invalidate()
    }

    private fun measurePie(width: Int, height: Int) {
        // TODO: 2/13/2021 Handle zero sizes.
        // Measure pie.
        val shortestLength = min(width, height)
        mBitmap?.recycle()
        try {
            mBitmap = Bitmap.createBitmap(shortestLength, shortestLength, Bitmap.Config.ARGB_8888)
            mCanvas = mBitmap?.let { Canvas(it) }
            mBitmapTop = (height / 2f) - ((mBitmap?.height ?: 0) / 2f)
            mBitmapLeft = (width / 2f) - ((mBitmap?.width ?: 0) / 2f)
            mSectorRect.set(
                0f, 0f,
                (mCanvas?.width?.toFloat() ?: 0f),
                (mCanvas?.height?.toFloat() ?: 0f)
            )
        } catch (e: IllegalArgumentException) {
            mCanvas = null
            mBitmap = null
            mBitmapTop = 0f
            mBitmapLeft = 0f
            mSectorRect.set(0f, 0f, 0f, 0f)
        }
        measureSectorGap()
    }

    private fun measureSectorGap() {
        // Scale sector bounds slightly larger than the pie to avoid drawing at the edge of
        // the pie during drawing sector gaps.
        val strokeWidth = spec.sectorGapThickness.toFloat()
        val halfStrokeWidth = strokeWidth / 2f
        val top = -halfStrokeWidth
        val left = -halfStrokeWidth
        val bottom = mSectorRect.bottom + halfStrokeWidth
        val right = mSectorRect.right + halfStrokeWidth
        mSectorGapRect.set(left, top, right, bottom)
        mSectorGapPaint.strokeWidth = strokeWidth
    }

    protected open fun onDrawChart(canvas: Canvas) {
        val sectors = (animatedData ?: data).sectors
        val sum = sectors.sumByDouble { it.value.toDouble() }
        var startAngle = 0f
        sectors.forEachIndexed { i, sector ->
            val sweepAngle = (sector.value / sum * 360f).toFloat()

            // Draw sectors.
            mSectorPaint.color = sector.color?.defaultColor ?: sectorShades[i].defaultColor
            canvas.drawArc(
                mSectorRect,
                startAngle, sweepAngle,
                true, mSectorPaint,
            )

            // Draw gaps.
            if (spec.sectorGapThickness > 0) {
                canvas.drawArc(
                    mSectorGapRect,
                    startAngle, sweepAngle,
                    true, mSectorGapPaint,
                )
            }

            startAngle += sweepAngle
        }
    }
}