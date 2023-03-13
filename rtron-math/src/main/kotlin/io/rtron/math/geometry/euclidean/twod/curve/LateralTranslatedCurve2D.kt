/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.geometry.euclidean.twod.curve

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.getOrElse
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.twod.point.CurveRelativeVector2D
import io.rtron.math.geometry.euclidean.twod.Rotation2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.transform.Affine2D

/**
 * Laterally translates a [baseCurve] by a [lateralTranslationFunction]. This enables for example the representation
 * of the [baseCurve] moved two units to the left.
 *
 * @param baseCurve base curve to be laterally translated
 * @param lateralTranslationFunction function which defines the lateral translation
 */
data class LateralTranslatedCurve2D(
    private val baseCurve: AbstractCurve2D,
    private val lateralTranslationFunction: UnivariateFunction,
    override val tolerance: Double
) : AbstractCurve2D() {

    // Properties and Initializers
    init {
        require(lateralTranslationFunction.domain.fuzzyEncloses(baseCurve.domain, tolerance)) { "The lateral translation function must be defined everywhere where the curve is also defined." }
    }

    override val domain: Range<Double> get() = baseCurve.domain

    // Methods

    override fun calculatePointLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Vector2D {

        val curveAffine = baseCurve.calculatePoseGlobalCSUnbounded(curveRelativePoint)
            .let { Affine2D.of(it) }

        val translation = calculateTranslation(curveRelativePoint).getOrElse { throw it }.lateralOffset

        return curveAffine.transform(Vector2D(0.0, translation))
    }

    override fun calculateRotationLocalCSUnbounded(curveRelativePoint: CurveRelativeVector1D): Rotation2D {

        val curveRotation = baseCurve.calculateRotationGlobalCSUnbounded(curveRelativePoint)
        val lateralTranslationSlope =
            lateralTranslationFunction.slopeInFuzzy(curveRelativePoint.curvePosition, tolerance)
                .getOrElse { throw it }
        val lateralTranslationRotation = Rotation2D(lateralTranslationSlope)
        return curveRotation + lateralTranslationRotation
    }

    /**
     * Returns a [LateralTranslatedCurve2D] with an additional translation of [lateralTranslationFunction].
     *
     * @param lateralTranslationFunction lateral translation function that is added
     * @param multiplier multiplication factor, whereby 0.5 means that only 0.5 of the translation function is added
     * @return resulting [LateralTranslatedCurve2D]
     */
    fun addLateralTranslation(lateralTranslationFunction: UnivariateFunction, multiplier: Double = 1.0):
        LateralTranslatedCurve2D {
        require(multiplier.isFinite()) { "Multiplier must be finite." }

        val lateralFunctions = listOf(this.lateralTranslationFunction, lateralTranslationFunction)
        val stacked = StackedFunction(lateralFunctions, { it[0] + multiplier * it[1] })
        return copy(lateralTranslationFunction = stacked)
    }

    /**
     * Returns the lateral translation at the [curveRelativePoint].
     */
    private fun calculateTranslation(curveRelativePoint: CurveRelativeVector1D):
        Either<Exception, CurveRelativeVector2D> = either.eager {

        val translation = lateralTranslationFunction
            .valueInFuzzy(curveRelativePoint.curvePosition, tolerance)
            .bind()
            .let { CurveRelativeVector2D(curveRelativePoint.curvePosition, it) }

        translation
    }
}
