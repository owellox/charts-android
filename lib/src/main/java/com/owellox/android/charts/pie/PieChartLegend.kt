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
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.DrawableCompat
import com.owellox.android.charts.R

class PieChartLegend(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int
) : AppCompatTextView(ContextThemeWrapper(context, defStyleAttr), attrs) {
    constructor(
        context: Context,
        attrs: AttributeSet,
    ) : this(context, attrs, R.style.Widget_Charts_PieChartLegend)

    companion object {
        @Suppress("unused")
        private const val TAG = "PieChartLegend"
    }

    private var icon: Drawable? = null
    private val spec: PieChartLegendSpec = PieChartLegendSpec(getContext(), attrs)

    private var iconLeft: Int = 0
    private var iconTop: Int = 0
    private var iconRight: Int = 0
    private var iconBottom: Int = 0

    init {
        loadIconFromAttributes(attrs)
    }

    override fun getCompoundPaddingLeft(): Int {
        return super.getCompoundPaddingLeft() + spec.iconSize
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val iconSize = spec.iconSize
        val width = measuredWidth
        val height = measuredHeight
        val midY = height / 2
        iconLeft = 0
        iconTop = midY - (iconSize / 2)
        iconRight = iconSize
        iconBottom = iconTop + iconSize
    }

    override fun onDraw(canvas: Canvas) {
        icon?.apply {
            DrawableCompat.setTintList(this, spec.iconTint)
            setBounds(iconLeft, iconTop, iconRight, iconBottom)
            draw(canvas)
        }
        super.onDraw(canvas)
    }

    private fun loadIconFromAttributes(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PieChartLegend, 0, 0
        ).apply {
            icon = getDrawable(R.styleable.PieChartLegend_legendIcon)
        }
        updateIcon()
    }

    private fun updateIcon() {
        icon = icon?.let { DrawableCompat.wrap(it).mutate() }
    }
}