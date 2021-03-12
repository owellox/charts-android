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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.core.view.children
import com.owellox.android.charts.R

/**
 * A special layout for [PieChart].
 */
class PieChartLayout : ViewGroup {
    companion object {
        @Suppress("unused")
        private const val TAG = "PieChartLayout"
    }

    private var mLegendsGravity: Int = Gravity.NO_GRAVITY

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        loadStyledAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private fun loadStyledAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PieChartLayout,
            0, 0
        ).apply {
            mLegendsGravity =
                getInt(R.styleable.PieChartLayout_legendsGravity, Gravity.NO_GRAVITY)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val pWidth = MeasureSpec.getSize(widthMeasureSpec)
        val pHeight = MeasureSpec.getSize(heightMeasureSpec)
        val pWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val pHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        val legendGroup = getLegendGroup()
        val legendGroupGravity = mLegendsGravity
        val hLegendGroupGravity = GravityCompat.getAbsoluteGravity(
            legendGroupGravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK,
            layoutDirection
        )

        @SuppressLint("RtlHardcoded")
        val hAligned = hLegendGroupGravity == Gravity.LEFT || hLegendGroupGravity == Gravity.RIGHT

        legendGroup?.also {
            // Limit legends width / height.
            val wSpec = if (hAligned) {
                MeasureSpec.makeMeasureSpec(legendGroup.measuredWidth, MeasureSpec.EXACTLY)
            } else widthMeasureSpec
            val hSpec = if (hAligned) {
                heightMeasureSpec
            } else MeasureSpec.makeMeasureSpec(legendGroup.measuredHeight, MeasureSpec.EXACTLY)
            measureChild(legendGroup, wSpec, hSpec)
        }

        val chart = getChart()
        chart?.also {
            // Used whatever space left.
            val wSpec = if (hAligned) {
                MeasureSpec.makeMeasureSpec(
                    pWidth - (legendGroup?.measuredWidth ?: 0),
                    MeasureSpec.EXACTLY
                )
            } else widthMeasureSpec
            val hSpec = if (hAligned) {
                heightMeasureSpec
            } else MeasureSpec.makeMeasureSpec(
                pHeight - (legendGroup?.measuredHeight ?: 0),
                MeasureSpec.EXACTLY
            )
            measureChild(chart, wSpec, hSpec)
        }
        val width = if (pWidthMode == MeasureSpec.EXACTLY) {
            pWidth
        } else {
            if (hAligned) {
                (chart?.measuredWidth ?: 0) + (legendGroup?.measuredWidth ?: 0)
            } else pWidth
        }
        val height = if (pHeightMode == MeasureSpec.EXACTLY) {
            pHeight
        } else {
            if (hAligned) pHeight
            else (chart?.measuredHeight ?: 0) + (legendGroup?.measuredHeight ?: 0)
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val legends = getLegendGroup()
        getChart()?.also { chart ->
            if (legends != null) {
                val chartGravity = layoutLegends(legends, mLegendsGravity)
                layoutChart(chart, chartGravity)
            } else layoutChart(chart)
        }
    }

    private fun layoutChart(view: View, gravity: Int = Gravity.NO_GRAVITY) {
        val width = view.measuredWidth
        val height = view.measuredHeight
        val hWidth = width / 2
        val hHeight = height / 2
        val midX = measuredWidth / 2
        val midY = measuredHeight / 2
        var left = midX - hWidth
        var top = midY - hHeight
        var right = midX + hWidth
        var bottom = midY + hHeight

        val absBottom = measuredHeight
        val absRight = measuredWidth

        val hGravity = GravityCompat.getAbsoluteGravity(
            gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK,
            layoutDirection
        )
        val vGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

        @SuppressLint("RtlHardcoded")
        val hAligned = hGravity == Gravity.LEFT || hGravity == Gravity.RIGHT
        val vAligned = vGravity == Gravity.TOP || vGravity == Gravity.BOTTOM

        when {
            vAligned -> {
                left = midX - hWidth
                right = midX + hWidth
                when (vGravity) {
                    Gravity.TOP -> {
                        left = midX - hWidth
                        top = 0
                        right = midX + hWidth
                        bottom = height
                    }
                    Gravity.BOTTOM -> {
                        left = midX - hWidth
                        top = absBottom - height
                        right = midX + hWidth
                        bottom = absBottom
                    }
                }
            }
            hAligned -> {
                top = midY - hHeight
                bottom = midY + hHeight
                @SuppressLint("RtlHardcoded")
                when (hGravity) {
                    Gravity.LEFT -> {
                        left = 0
                        top = midY - hHeight
                        right = width
                        bottom = midY + hHeight
                    }
                    Gravity.RIGHT -> {
                        left = absRight - width
                        top = midY - hHeight
                        right = absRight
                        bottom = midY + hHeight
                    }
                }
            }
            else -> {
                left = midX - hWidth
                top = midY - hHeight
                right = midX + hWidth
                bottom = midY + hHeight
            }
        }

        view.layout(left, top, right, bottom)
    }

    private fun layoutLegends(view: View, gravity: Int): Int {
        val hGravity = GravityCompat.getAbsoluteGravity(
            gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK,
            layoutDirection
        )
        val vGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
        val width = view.measuredWidth
        val height = view.measuredHeight
        val hWidth = width / 2
        val hHeight = height / 2

        val absBottom = measuredHeight
        val absRight = measuredWidth
        val midY = absBottom / 2
        val midX = absRight / 2

        // Default legends position to vertical bottom.
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        @SuppressLint("RtlHardcoded")
        val hAligned = hGravity == Gravity.LEFT || hGravity == Gravity.RIGHT
        val vAligned = vGravity == Gravity.TOP || vGravity == Gravity.BOTTOM

        try {
            @SuppressLint("RtlHardcoded")
            when {
                vAligned -> {
                    left = midX - hWidth
                    right = midX + hWidth
                    return when (vGravity) {
                        Gravity.TOP -> {
                            top = 0
                            bottom = height
                            Gravity.BOTTOM
                        }
                        Gravity.BOTTOM -> {
                            top = absBottom - height
                            bottom = absBottom
                            Gravity.TOP
                        }
                        else -> Gravity.NO_GRAVITY
                    }
                }
                hAligned -> {
                    top = midY - hHeight
                    bottom = midY + hHeight
                    return when (hGravity) {
                        Gravity.LEFT -> {
                            left = 0
                            right = width
                            Gravity.RIGHT
                        }
                        Gravity.RIGHT -> {
                            left = absRight - width
                            right = absRight
                            Gravity.LEFT
                        }
                        else -> Gravity.NO_GRAVITY
                    }
                }
                else -> {
                    // Default to bottom legends.
                    left = midX - hWidth
                    top = absBottom - height
                    right = midX + hWidth
                    bottom = absBottom
                    return Gravity.TOP
                }
            }
        } finally {
            view.layout(left, top, right, bottom)
        }
    }

    private fun getChart(): PieChart? {
        return children.filterIsInstance<PieChart>().firstOrNull()
    }

    private fun getLegendGroup(): PieChartLegendGroup? {
        return children.filterIsInstance<PieChartLegendGroup>().firstOrNull()
    }
}