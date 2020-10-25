/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.roadspace2citygml.geometry

import com.github.kittinunf.result.Result
import io.rtron.io.logging.Logger
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.AbstractSolid3D
import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.Circle3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlParameters
import io.rtron.transformer.roadspace2citygml.transformer.IdentifierAdder
import org.citygml4j.factory.GMLGeometryFactory
import org.citygml4j.model.gml.geometry.GeometryProperty
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface
import org.citygml4j.model.gml.geometry.primitives.*
import java.util.*


/**
 * Generates a surface based geometry representation for CityGML by visiting the geometry class.
 *
 * @param parameters parameters for the geometry transformation, such as discretization step sizes
 */
class GeometryTransformer(
        val parameters: Roadspaces2CitygmlParameters,
        private val reportLogger: Logger
) : Geometry3DVisitor {

    // Properties and Initializers
    private val _identifierAdder = IdentifierAdder(parameters, reportLogger)

    lateinit var solidProperty: SolidProperty
        private set
    lateinit var multiSurfaceProperty: MultiSurfaceProperty
        private set
    lateinit var lineStringProperty: LineStringProperty
        private set
    lateinit var pointProperty: PointProperty
        private set

    lateinit var rotation: Rotation3D
        private set

    var height: Double = Double.NaN
        private set
    var diameter: Double = Double.NaN
        private set

    // Methods
    fun getSolidProperty(): Result<SolidProperty, IllegalStateException> =
            if (isSetSolid()) Result.success(solidProperty)
            else Result.error(IllegalStateException("No SolidProperty available for geometry."))

    fun getMultiSurfaceProperty(): Result<MultiSurfaceProperty, IllegalStateException> =
            if (isSetMultiSurface()) Result.success(multiSurfaceProperty)
            else Result.error(IllegalStateException("No MultiSurfaceProperty available for geometry."))

    fun getLineStringProperty(): Result<LineStringProperty, IllegalStateException> =
            if (isSetLineString()) Result.success(lineStringProperty)
            else Result.error(IllegalStateException("No LineStringProperty available for geometry."))

    fun getPointProperty(): Result<PointProperty, IllegalStateException> =
            if (isSetPoint()) Result.success(pointProperty)
            else Result.error(IllegalStateException("No PointProperty available for geometry."))

    /**
     * Returns the available corresponding CityGML [GeometryProperty] in the prioritization order: solid,
     * multi surface, line string and point
     */
    fun getGeometryProperty(): Result<GeometryProperty<*>, IllegalStateException> =
            when {
                isSetSolid() -> getSolidProperty()
                isSetMultiSurface() -> getMultiSurfaceProperty()
                isSetLineString() -> getLineStringProperty()
                isSetPoint() -> getPointProperty()
                else -> Result.error(IllegalStateException("No adequate geometry found."))
            }

    fun getRotation(): Result<Rotation3D, IllegalStateException> =
            if (isSetRotation()) Result.success(rotation)
            else Result.error(IllegalStateException("No rotation available."))

    private fun isSetSolid() = this::solidProperty.isInitialized && solidProperty.isSetGeometry
    private fun isSetMultiSurface() = this::multiSurfaceProperty.isInitialized && multiSurfaceProperty.isSetGeometry
    private fun isSetLineString() = this::lineStringProperty.isInitialized && lineStringProperty.isSetGeometry
    private fun isSetPoint() = this::pointProperty.isInitialized && pointProperty.isSetGeometry

    fun isSetRotation() = this::rotation.isInitialized

    fun isSetHeight() = !height.isNaN()
    fun isSetDiameter() = !diameter.isNaN()


    override fun visit(vector3D: Vector3D) {
        val vectorGlobalCS = vector3D.calculatePointGlobalCS()
        val directPosition = geometryFactory
                .createDirectPosition(vectorGlobalCS.toDoubleArray(), DIMENSION)!!
        val point = Point().apply {
            pos = directPosition
            if (parameters.generateRandomGeometryIds) id = _identifierAdder.generateRandomUUID()
        }
        this.pointProperty = PointProperty(point)
        visit(vector3D as AbstractGeometry3D)
    }

    override fun visit(abstractCurve3D: AbstractCurve3D) {

        val points = abstractCurve3D.calculatePointListGlobalCS(parameters.discretizationStepSize)
                .handleFailure { throw it.error }
                .ifEmpty { return }

        val coordinatesList = points.flatMap { it.toDoubleList() }
        val lineString = geometryFactory.createLineString(coordinatesList, DIMENSION)!!
        this.lineStringProperty = LineStringProperty(lineString)
    }

    override fun visit(abstractSurface3D: AbstractSurface3D) {
        abstractSurface3D.calculatePolygonsGlobalCS().fold(
                { polygonsToMultiSurfaceRepresentation(it) },
                { reportLogger.log(it) }
        )
        visit(abstractSurface3D as AbstractGeometry3D)
    }

    override fun visit(circle3D: Circle3D) {
        val adjustedCircle = circle3D.copy(numberSlices = parameters.circleSlices)
        visit(adjustedCircle as AbstractSurface3D)
        visit(circle3D as AbstractGeometry3D)
    }

    override fun visit(abstractSolid3D: AbstractSolid3D) {
        abstractSolid3D.calculatePolygonsGlobalCS().fold(
                { polygonsToSolidRepresentation(it) },
                { reportLogger.log(it) }
        )
        visit(abstractSolid3D as AbstractGeometry3D)
    }

    override fun visit(cylinder3D: Cylinder3D) {
        this.height = cylinder3D.height
        this.diameter = cylinder3D.diameter
        val adjustedCylinder = cylinder3D.copy(numberSlices = parameters.circleSlices)
        visit(adjustedCylinder as AbstractSolid3D)
    }

    override fun visit(parametricSweep3D: ParametricSweep3D) {
        val adjustedParametricSweep3D = parametricSweep3D
                .copy(discretizationStepSize = parameters.sweepDiscretizationStepSize)
        visit(adjustedParametricSweep3D as AbstractSolid3D)
    }

    override fun visit(abstractGeometry3D: AbstractGeometry3D) {
        this.rotation = abstractGeometry3D.affineSequence.solve().extractRotation()
    }

    private fun polygonsToSolidRepresentation(polygons: List<Polygon3D>) {
        val surfaceMembers = ArrayList<SurfaceProperty>()

        polygons.forEach {
            val polygonGml = geometryFactory.createLinearPolygon(it.toVertexPositionElementList(), DIMENSION)!!
            if (parameters.generateRandomGeometryIds) polygonGml.id = _identifierAdder.generateRandomUUID()
            surfaceMembers.add(SurfaceProperty(polygonGml))
        }
        val compositeSurface = CompositeSurface().apply {
            if (parameters.generateRandomGeometryIds) id = _identifierAdder.generateRandomUUID()
            surfaceMember = surfaceMembers
        }

        val solid = Solid().apply {
            if (parameters.generateRandomGeometryIds) id = _identifierAdder.generateRandomUUID()
            exterior = SurfaceProperty(compositeSurface)
        }
        this.solidProperty = SolidProperty(solid)
    }

    private fun polygonsToMultiSurfaceRepresentation(polygons: List<Polygon3D>) {
        val multiSurface = MultiSurface().apply {
            if (parameters.generateRandomGeometryIds) id = _identifierAdder.generateRandomUUID()
        }
        polygons.forEach {
            val polygonGml = geometryFactory.createLinearPolygon(it.toVertexPositionElementList(), DIMENSION)!!
            if (parameters.generateRandomGeometryIds) polygonGml.id = _identifierAdder.generateRandomUUID()
            multiSurface.addSurfaceMember(SurfaceProperty(polygonGml))
        }
        this.multiSurfaceProperty = MultiSurfaceProperty(multiSurface)
    }

    companion object {
        private val geometryFactory = GMLGeometryFactory()
        private const val DIMENSION = 3
    }
}
