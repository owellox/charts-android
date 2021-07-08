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

import androidx.annotation.RestrictTo
import com.owellox.android.charts.data.Point
import kotlin.math.max

class LineChartData
internal constructor(
    internal val lines: List<LineChartLine>,
    internal val spec: Spec,
    internal val config: Config = Config(),
) {
    companion object {
        @JvmStatic
        fun from(lines: List<LineChartLine>): LineChartData {
            val minX = lines.minOfOrNull { it.minX } ?: 0f
            val maxX = lines.maxOfOrNull { it.maxX } ?: max(minX, 0f)
            val minY = lines.minOfOrNull { it.minY } ?: 0f
            val maxY = lines.maxOfOrNull { it.maxY } ?: max(minY, 0f)
            val spec = Spec(
                minX = minX, maxX = maxX,
                minY = minY, maxY = maxY,
            )
            return LineChartData(lines, spec = spec)
        }

        @JvmStatic
        fun from(vararg lines: LineChartLine): LineChartData {
            return from(lines.toList())
        }

        @JvmStatic
        fun empty(): LineChartData {
            return from(emptyList())
        }

        @JvmStatic
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        fun editModeData(): LineChartData {
            val line1 = LineChartLine.Builder()
                .apply {
                    add(Point(0f, .8f))
                    add(Point(4f, 0f))
                    add(Point(4.8f, .6f))
                    add(Point(7.57f, .5f))
                    add(Point(7.76f, .55f))
                    add(Point(8f, .86f))
                    add(Point(9f, 1f))
                    add(Point(10f, .97f))
                }.build()
            val line2 = LineChartLine.Builder().build()
            return from(line1, line2)
        }
    }

    internal class Spec(
        internal val minX: Float, internal val maxX: Float,
        internal val minY: Float, internal val maxY: Float,
    )

    data class Config(
        val baseX: Float? = null,
        val baseY: Float? = null,
    )
}