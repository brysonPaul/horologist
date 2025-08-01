/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMetricApi::class)

package com.google.android.horologist.media.benchmark.metrics

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.TraceMetric
import androidx.benchmark.traceprocessor.TraceProcessor
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

public class CompositionMetric(private val composable: String) : TraceMetric() {
    override fun getMeasurements(
        captureInfo: CaptureInfo,
        traceSession: TraceProcessor.Session,
    ): List<Measurement> {
        val shortName = composable.substringAfterLast(".")

        val durationsNs = traceSession.query(
            """
                SELECT * FROM slice
                    INNER JOIN thread_track on slice.track_id = thread_track.id
                    INNER JOIN thread USING(utid)
                    INNER JOIN process USING(upid)
                WHERE process.name LIKE "${captureInfo.targetPackageName}"
                    AND slice.name LIKE "$composable (%)"
            """.trimIndent(),
        ).map { it.long("dur") }

        return listOf(
            Measurement(
                "${shortName}RecomposeDurMs",
                durationsNs.sumOf { it }.nanoseconds.toDouble(DurationUnit.MILLISECONDS),
            ),
            Measurement("${shortName}RecomposeCount", durationsNs.count().toDouble()),
        )
    }
}
