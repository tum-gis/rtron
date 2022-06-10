/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.evaluator.opendrive.plans.basicdatatype

import arrow.core.None
import arrow.core.some
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Report
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.model.opendrive.additions.optics.everyRoadObjectOutlineElement
import io.rtron.model.opendrive.additions.optics.everyRoadObjectRepeatElement
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.opendrive.report.toMessage

class RoadObjectsEvaluator(val configuration: OpendriveEvaluatorConfiguration) {

    // Methods
    fun evaluateFatalViolations(opendriveModel: OpendriveModel): Report {
        val report = Report()
        return report
    }

    fun evaluateNonFatalViolations(opendriveModel: OpendriveModel): ContextReport<OpendriveModel> {
        val report = Report()
        var healedOpendriveModel = opendriveModel

        healedOpendriveModel = everyRoadObject.modify(healedOpendriveModel) { currentRoadObject ->

            if (!currentRoadObject.s.isFinite() || currentRoadObject.s < 0.0) {
                report += OpendriveException.UnexpectedValue("s", currentRoadObject.s.toString(), "Value shall be finite and greater equals zero.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.s = 0.0
            }
            if (!currentRoadObject.t.isFinite()) {
                report += OpendriveException.UnexpectedValue("t", currentRoadObject.t.toString(), "Value shall be finite.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.t = 0.0
            }
            if (!currentRoadObject.zOffset.isFinite()) {
                report += OpendriveException.UnexpectedValue("zOffset", currentRoadObject.zOffset.toString(), "Value shall be finite.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.zOffset = 0.0
            }

            if (currentRoadObject.hdg.exists { !it.isFinite() }) {
                report += OpendriveException.UnexpectedValue("hdg", currentRoadObject.hdg.toString(), "Value shall be finite.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.hdg = None
            }

            if (currentRoadObject.roll.exists { !it.isFinite() }) {
                report += OpendriveException.UnexpectedValue("roll", currentRoadObject.roll.toString(), "Value shall be finite.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.roll = None
            }

            if (currentRoadObject.pitch.exists { !it.isFinite() }) {
                report += OpendriveException.UnexpectedValue("pitch", currentRoadObject.pitch.toString(), "Value shall be finite.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.roll = None
            }

            if (currentRoadObject.height.exists { !it.isFinite() || it < 0.0 }) {
                report += OpendriveException.UnexpectedValue("height", currentRoadObject.height.toString(), "Value shall be finite and greater equals zero.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.height = None
            }

            if (currentRoadObject.height.exists { 0.0 < it && it < configuration.numberTolerance }) {
                currentRoadObject.height = configuration.numberTolerance.some()
            }

            if (currentRoadObject.radius.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("radius", currentRoadObject.radius.toString(), "Value shall be finite and greater zero.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.radius = None
            }

            if (currentRoadObject.length.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("length", currentRoadObject.length.toString(), "Value shall be finite and greater zero.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.length = None
            }

            if (currentRoadObject.width.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("width", currentRoadObject.width.toString(), "Value shall be finite and greater zero.").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.width = None
            }

            if (currentRoadObject.outlines.exists { it.outline.isEmpty() }) {
                report += OpendriveException.EmptyValueForOptionalAttribute("outlines").toMessage(currentRoadObject.additionalId, isFatal = true, wasHealed = false)
                currentRoadObject.outlines = None
            }

            val repeatElementsFiltered = currentRoadObject.repeat.filter { it.s.isFinite() && it.tStart.isFinite() && it.zOffsetStart.isFinite() }
            if (repeatElementsFiltered.size < currentRoadObject.repeat.size) {
                report += OpendriveException.UnexpectedValues("s, tStart, zOffsetStart", "Ignoring ${currentRoadObject.repeat.size - repeatElementsFiltered.size} repeat entries which do not have a finite s, tStart and zOffsetStart value.").toMessage(currentRoadObject.additionalId, isFatal = false, wasHealed = true)
                currentRoadObject.repeat = repeatElementsFiltered
            }

            currentRoadObject
        }

        healedOpendriveModel = everyRoadObjectOutlineElement.modify(healedOpendriveModel) { currentOutlineElement ->

            val cornerRoadElementsFiltered = currentOutlineElement.cornerRoad.filter { it.s.isFinite() && it.t.isFinite() && it.dz.isFinite() }
            if (cornerRoadElementsFiltered.size < currentOutlineElement.cornerRoad.size) {
                report += OpendriveException.UnexpectedValues("s, t, dz", "Ignoring ${currentOutlineElement.cornerRoad.size - cornerRoadElementsFiltered.size} cornerRoad entries which do not have a finite s, t and dz value.").toMessage(currentOutlineElement.additionalId, isFatal = false, wasHealed = true)
                currentOutlineElement.cornerRoad = cornerRoadElementsFiltered
            }

            currentOutlineElement.cornerRoad.forEach { currentCornerRoad ->
                if (!currentCornerRoad.height.isFinite() || currentCornerRoad.height < 0.0) {
                    report += OpendriveException.UnexpectedValue("height", currentCornerRoad.height.toString(), "Value shall be finite and greater equals zero.").toMessage(currentOutlineElement.additionalId, isFatal = true, wasHealed = false)
                    currentCornerRoad.height = 0.0
                }

                if (0.0 < currentCornerRoad.height && currentCornerRoad.height <= configuration.numberTolerance) {
                    currentCornerRoad.height = 0.0
                }
            }

            val cornerLocalElementsFiltered = currentOutlineElement.cornerLocal.filter { it.u.isFinite() && it.v.isFinite() && it.z.isFinite() }
            if (cornerLocalElementsFiltered.size < currentOutlineElement.cornerLocal.size) {
                report += OpendriveException.UnexpectedValues("s, t, dz", "Ignoring ${currentOutlineElement.cornerLocal.size - cornerLocalElementsFiltered.size} cornerLocal entries which do not have a finite u, v and z value.").toMessage(currentOutlineElement.additionalId, isFatal = false, wasHealed = true)
                currentOutlineElement.cornerLocal = cornerLocalElementsFiltered
            }

            currentOutlineElement.cornerLocal.forEach { currentCornerLocal ->
                if (!currentCornerLocal.height.isFinite() || currentCornerLocal.height < 0.0) {
                    report += OpendriveException.UnexpectedValue("height", currentCornerLocal.height.toString(), "Value shall be finite and greater equals zero.").toMessage(currentOutlineElement.additionalId, isFatal = true, wasHealed = false)
                    currentCornerLocal.height = 0.0
                }

                if (0.0 < currentCornerLocal.height && currentCornerLocal.height <= configuration.numberTolerance) {
                    currentCornerLocal.height = 0.0
                }
            }

            currentOutlineElement
        }

        healedOpendriveModel = everyRoadObjectRepeatElement.modify(healedOpendriveModel) { currentRepeatElement ->
            require(currentRepeatElement.s.isFinite()) { "Must already be filtered." }
            require(currentRepeatElement.tStart.isFinite()) { "Must already be filtered." }
            require(currentRepeatElement.zOffsetStart.isFinite()) { "Must already be filtered." }

            if (!currentRepeatElement.distance.isFinite() || currentRepeatElement.distance < 0.0) {
                report += OpendriveException.UnexpectedValue("distance", currentRepeatElement.distance.toString(), "Value shall be finite and greater equals zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.distance = 0.0
            }
            if (!currentRepeatElement.heightEnd.isFinite() || currentRepeatElement.heightEnd < 0.0) {
                report += OpendriveException.UnexpectedValue("heightEnd", currentRepeatElement.heightEnd.toString(), "Value shall be finite and greater equals zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.heightEnd = 0.0
            }
            if (!currentRepeatElement.heightStart.isFinite() || currentRepeatElement.heightStart < 0.0) {
                report += OpendriveException.UnexpectedValue("heightStart", currentRepeatElement.heightStart.toString(), "Value shall be finite and greater equals zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.heightStart = 0.0
            }
            if (!currentRepeatElement.length.isFinite() || currentRepeatElement.length < 0.0) {
                report += OpendriveException.UnexpectedValue("length", currentRepeatElement.length.toString(), "Value shall be finite and greater equals zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.length = 0.0
            }
            if (currentRepeatElement.lengthEnd.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("lengthEnd", currentRepeatElement.lengthEnd.toString(), "Value shall be finite and greater zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.lengthEnd = None
            }
            if (currentRepeatElement.lengthStart.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("lengthStart", currentRepeatElement.lengthStart.toString(), "Value shall be finite and greater zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.lengthStart = None
            }
            if (currentRepeatElement.radiusEnd.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("radiusEnd", currentRepeatElement.radiusEnd.toString(), "Value shall be finite and greater zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.radiusEnd = None
            }
            if (currentRepeatElement.radiusStart.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("radiusStart", currentRepeatElement.radiusStart.toString(), "Value shall be finite and greater zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.radiusStart = None
            }
            if (!currentRepeatElement.tEnd.isFinite()) {
                report += OpendriveException.UnexpectedValue("tEnd", currentRepeatElement.tEnd.toString(), "Value shall be finite.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.tEnd = currentRepeatElement.tStart
            }
            if (currentRepeatElement.widthEnd.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("widthEnd", currentRepeatElement.widthEnd.toString(), "Value shall be finite and greater zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.widthEnd = None
            }
            if (currentRepeatElement.widthStart.exists { !it.isFinite() || it <= configuration.numberTolerance }) {
                report += OpendriveException.UnexpectedValue("widthStart", currentRepeatElement.widthStart.toString(), "Value shall be finite and greater zero.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.widthStart = None
            }
            if (!currentRepeatElement.zOffsetEnd.isFinite()) {
                report += OpendriveException.UnexpectedValue("zOffsetEnd", currentRepeatElement.zOffsetEnd.toString(), "Value shall be finite.").toMessage(currentRepeatElement.additionalId, isFatal = true, wasHealed = false)
                currentRepeatElement.zOffsetEnd = currentRepeatElement.zOffsetStart
            }

            currentRepeatElement
        }

        return ContextReport(healedOpendriveModel, report)
    }
}
