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
import androidx.annotation.Px
import com.owellox.android.charts.R

class PieChartSpec
internal constructor(context: Context, attrs: AttributeSet?) {
    @Px
    var chartSize: Int = 0

    @Px
    var sectorGapThickness: Int = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PieChart, 0, 0
        ).apply {
            sectorGapThickness = getDimensionPixelSize(
                R.styleable.PieChart_sectorGap,
                (2f * resources.displayMetrics.density).toInt()
            )
            chartSize = getDimensionPixelSize(R.styleable.PieChart_chartSize, 0)
        }
    }
}