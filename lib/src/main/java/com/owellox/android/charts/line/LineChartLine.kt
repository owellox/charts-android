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

import android.graphics.Color
import com.owellox.android.charts.data.Point
import kotlin.math.max
import kotlin.math.min

class LineChartLine private constructor(
    internal val points: List<Point>,
    internal val minX: Float,
    internal val minY: Float,
    internal val maxX: Float,
    internal val maxY: Float,
    internal val color: Int,
) {
    class Builder(private val color: Int = Color.BLACK) {
        private val points = mutableListOf<Point>()

        private var minX = 0f
        private var maxX = 0f

        private var minY = 0f
        private var maxY = 0f

        fun add(point: Point): Builder {
            minX = min(minX, point.x)
            maxX = max(maxX, point.x)

            minY = min(minY, point.y)
            maxY = max(maxY, point.y)

            points.add(point)

            return this
        }

        fun build(): LineChartLine {
            return LineChartLine(
                points.sortedBy { it.x },
                minX, minY, maxX, maxY,
                color,
            )
        }
    }
}