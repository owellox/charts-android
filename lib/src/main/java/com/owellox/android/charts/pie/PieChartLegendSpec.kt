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
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.annotation.Px
import com.owellox.android.charts.R

class PieChartLegendSpec
internal constructor(context: Context, attrs: AttributeSet?) {
    @Px
    var iconSize: Int = 0
    var iconTint: ColorStateList? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PieChartLegend, 0, 0
        ).apply {
            iconSize = getDimensionPixelSize(R.styleable.PieChartLegend_legendIconSize, 0)
            iconTint = getColorStateList(R.styleable.PieChartLegend_legendIconTint)
        }
    }
}