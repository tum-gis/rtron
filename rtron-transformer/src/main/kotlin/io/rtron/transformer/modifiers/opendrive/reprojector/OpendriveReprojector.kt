/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.modifiers.opendrive.reprojector

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.toOption
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.linear.RealMatrix
import io.rtron.math.linear.SingularValueDecomposition
import io.rtron.math.processing.calculateCentroid
import io.rtron.math.transform.Affine2D
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.extensions.updateAdditionalIdentifiers
import io.rtron.model.opendrive.additions.optics.everyRoadPlanViewGeometry
import io.rtron.model.opendrive.core.HeaderGeoReference
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

class OpendriveReprojector(
    val parameters: OpendriveReprojectorParameters,
) {
    // Properties and Initializers
    private val logger = KotlinLogging.logger {}

    // Methods

    fun modify(opendriveModel: OpendriveModel): Pair<Option<OpendriveModel>, OpendriveReprojectorReport> {
        val report = OpendriveReprojectorReport(parameters)
        var modifiedOpendriveModel = opendriveModel.copy()
        modifiedOpendriveModel.updateAdditionalIdentifiers()

        if (!parameters.reprojectModel) {
            report.message = "No reprojection configured."
            return Some(modifiedOpendriveModel) to report
        }
        if (parameters.getTargetCrsEpsg().isNone()) {
            report.message = "No target coordinate reference system EPSG code configured."
            return None to report
        }
        if (modifiedOpendriveModel.header.geoReference.isNone()) {
            report.message = "No source georeference defined in OpenDRIVE model."
            return None to report
        }

        // setup coordinate transform
        val crsFactory = CRSFactory()
        val sourceCrs: CoordinateReferenceSystem =
            try {
                crsFactory.createFromParameters(
                    "source",
                    modifiedOpendriveModel.header.geoReference.getOrNull()!!.content,
                )
            } catch (e: Exception) {
                report.message = "Source CRS could not be identified: " + e.message.toOption().getOrElse { "" }
                return None to report
            }
        val targetCrs = crsFactory.createFromName("epsg:${parameters.targetCrsEpsg}")
        val ctFactory = CoordinateTransformFactory()
        val coordinateTransform: CoordinateTransform = ctFactory.createTransform(sourceCrs, targetCrs)

        // reproject extracted geometry points
        val planViewSourceCoordinates =
            modifiedOpendriveModel.road
                .flatMap { it.planView.geometry }
                .map { ProjCoordinate(it.x, it.y) }
        val planViewTargetCoordinates =
            planViewSourceCoordinates.map {
                val result = ProjCoordinate()
                coordinateTransform.transform(it, result)
                result
            }

        // estimate rigid affine transform
        val (affine, maximumDeviation) = estimateRigidAffine(planViewSourceCoordinates, planViewTargetCoordinates)
        report.maximumDeviation = maximumDeviation
        if (maximumDeviation > parameters.deviationWarningTolerance) {
            logger.warn {
                "Maximum deviation is $maximumDeviation and thus exceeding the warning threshold " +
                    "of ${parameters.deviationWarningTolerance}."
            }
        }

        // set new georeference and apply affine transform to the model
        modifiedOpendriveModel.header.geoReference = Some(HeaderGeoReference(targetCrs.parameterString))
        everyRoadPlanViewGeometry.modify(modifiedOpendriveModel) { currentPlanViewGeometry ->
            val transformedPose = affine.transform(currentPlanViewGeometry.getPose())
            currentPlanViewGeometry.x = transformedPose.point.x
            currentPlanViewGeometry.y = transformedPose.point.y
            currentPlanViewGeometry.hdg = transformedPose.rotation.angle
            currentPlanViewGeometry
        }

        report.message = "Reprojection of translation with ${affine.extractTranslation()} and " +
            "rotation with ${affine.extractRotation()} successfully applied. "
        return Some(modifiedOpendriveModel) to report
    }

    /**
     * Estimates a rigid affine matrix from two lists of corresponding coordinates.
     * It uses a singular value decomposition for estimating the rotation.
     *
     * Further details can be found [here](https://nghiaho.com/?page_id=671).
     */
    private fun estimateRigidAffine(
        sourceCoordinates: List<ProjCoordinate>,
        targetCoordinates: List<ProjCoordinate>,
    ): Pair<Affine2D, Double> {
        val sourcePoints = sourceCoordinates.map { Vector2D(it.x, it.y) }
        val targetPoints = targetCoordinates.map { Vector2D(it.x, it.y) }

        // calculate centriods and local
        val sourcePointsCentroid = sourcePoints.calculateCentroid()
        val localSourcePoints = sourcePoints.map { it - sourcePointsCentroid }
        val targetPointsCentroid = targetPoints.calculateCentroid()
        val localTargetPoints = targetPoints.map { it - targetPointsCentroid }

        // calculate covariance
        val sourceMatrix = RealMatrix.of(localSourcePoints).transpose()
        val targetMatrix = RealMatrix.of(localTargetPoints)
        val covariance = sourceMatrix.multiply(targetMatrix)

        // estimate affine transform matrix
        val svd = SingularValueDecomposition(covariance)
        val rotationAffine = Affine2D.of(svd.matrixVT.multiply(svd.matrixU.transpose()))
        val translationAffine = Affine2D.of(targetPointsCentroid - rotationAffine.transform(sourcePointsCentroid))
        val affine = translationAffine.append(rotationAffine)

        // determine maximum deviation between rigid affine and geospatial projection
        val maximumDeviation =
            sourcePoints.zip(targetPoints).maxOfOrNull {
                affine.transform(it.first).distance(it.second)
            }!!

        return affine to maximumDeviation
    }
}
