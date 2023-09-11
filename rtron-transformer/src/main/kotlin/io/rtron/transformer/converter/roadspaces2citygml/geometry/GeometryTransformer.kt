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

package io.rtron.transformer.converter.roadspaces2citygml.geometry

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.nonEmptyListOf
import arrow.core.right
import arrow.core.some
import arrow.core.toNonEmptyListOrNone
import arrow.core.toNonEmptyListOrNull
import io.rtron.math.geometry.GeometryException
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.curve.LineString3D
import io.rtron.math.geometry.euclidean.threed.point.AbstractPoint3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.AbstractSolid3D
import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.Circle3D
import io.rtron.math.geometry.euclidean.threed.surface.ParametricBoundedSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.std.QUARTER_PI
import io.rtron.math.std.THREE_QUARTER_PI
import io.rtron.math.transform.Affine3D
import io.rtron.std.handleEmpty
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.transformer.generateRandomUUID
import org.citygml4j.core.model.core.ImplicitGeometry
import org.citygml4j.core.model.core.ImplicitGeometryProperty
import org.citygml4j.core.util.geometry.GeometryFactory
import org.xmlobjects.gml.model.geometry.DirectPosition
import org.xmlobjects.gml.model.geometry.aggregates.MultiCurve
import org.xmlobjects.gml.model.geometry.aggregates.MultiCurveProperty
import org.xmlobjects.gml.model.geometry.aggregates.MultiSurface
import org.xmlobjects.gml.model.geometry.aggregates.MultiSurfaceProperty
import org.xmlobjects.gml.model.geometry.primitives.CurveProperty
import org.xmlobjects.gml.model.geometry.primitives.Point
import org.xmlobjects.gml.model.geometry.primitives.PointProperty
import org.xmlobjects.gml.model.geometry.primitives.Shell
import org.xmlobjects.gml.model.geometry.primitives.Solid
import org.xmlobjects.gml.model.geometry.primitives.SolidProperty
import org.xmlobjects.gml.model.geometry.primitives.SurfaceProperty

/**
 * Generates a surface based geometry representation for CityGML by visiting the geometry class.
 *
 * @param parameters parameters for the geometry transformation, such as discretization step sizes
 */
class GeometryTransformer(
    val parameters: Roadspaces2CitygmlParameters
) : Geometry3DVisitor {

    // Properties and Initializers
    private var polygonsOfSolidResult: Option<NonEmptyList<Polygon3D>> = None
    private var polygonsOfSurfaceResult: Option<Either<GeometryException.BoundaryRepresentationGenerationError, List<Polygon3D>>> =
        None
    private var multiCurveResult: Option<Either<GeometryException, LineString3D>> = None
    private var pointResult: Option<Vector3D> = None

    var rotation: Option<Rotation3D> = None
        private set
    var height: Option<Double> = None
        private set
    var diameter: Option<Double> = None
        private set

    // Methods

    /**
     * Returns the result of the created solid geometry. If no solid geometry is available, an [IllegalStateException] is thrown.
     */
    fun getSolid(): Option<SolidProperty> {
        val polygonsOfSolid = polygonsOfSolidResult.handleEmpty { return None }

        val gmlPolygons = polygonsOfSolid.map {
            val polygonGml = geometryFactory.createPolygon(it.toVertexPositionElementList(), DIMENSION)!!
            if (parameters.generateRandomGeometryIds) polygonGml.id = generateRandomUUID(parameters.gmlIdPrefix)
            SurfaceProperty(polygonGml)
        }

        val solid = Solid(Shell(gmlPolygons))
        if (parameters.generateRandomGeometryIds) solid.id = generateRandomUUID(parameters.gmlIdPrefix)

        return SolidProperty(solid).some()
    }

    /**
     * Returns the result of the created multi surface geometry. If no multi surface geometry is available, an [IllegalStateException] is thrown.
     */
    fun getMultiSurface(): Option<Either<GeometryException.BoundaryRepresentationGenerationError, MultiSurfaceProperty>> {
        val polygonsOfSurface = polygonsOfSurfaceResult.handleEmpty { return None }
        return polygonsOfSurface.map { polygonsToMultiSurfaceProperty(it.toNonEmptyListOrNull()!!) }.some()
    }

    /**
     * Returns the result of the created multi curve geometry. If no multi curve geometry is available, an [IllegalStateException] is thrown.
     */
    fun getMultiCurve(): Option<Either<GeometryException, MultiCurveProperty>> {
        val lineString = multiCurveResult.handleEmpty { return None }

        return lineString.map {
            val coordinatesList = it.vertices.flatMap { it.toDoubleList() }
            val gmlLineString = geometryFactory.createLineString(coordinatesList, DIMENSION)!!
            val curveProperty = CurveProperty(gmlLineString)
            val multiCurve = MultiCurve(listOf(curveProperty))

            MultiCurveProperty(multiCurve)
        }.some()
    }

    /**
     * Returns the result of the created point geometry. If no point geometry is available, an [IllegalStateException] is thrown.
     */
    fun getPoint(): Option<PointProperty> {
        val point = pointResult.handleEmpty { return None }

        val gmlPoint = Point().apply {
            pos = createDirectPosition(point)
            if (parameters.generateRandomGeometryIds) id = generateRandomUUID(parameters.gmlIdPrefix)
        }
        return PointProperty(gmlPoint).some()
    }

    /**
     * Returns the result of the created implicit geometry. If no implicit geometry is available, an [IllegalStateException] is thrown.
     */
    fun getImplicitGeometry(): Option<ImplicitGeometryProperty> {
        val point = getPoint().handleEmpty { return None }

        val implicitGeometry = ImplicitGeometry()
        implicitGeometry.referencePoint = point
        if (parameters.generateRandomGeometryIds) {
            implicitGeometry.id = generateRandomUUID(parameters.gmlIdPrefix)
        }

        // implicitGeometry.libraryObject = ""
        rotation.onSome {
            implicitGeometry.transformationMatrix = Affine3D.of(it).toGmlTransformationMatrix4x4()
        }

        return ImplicitGeometryProperty(implicitGeometry).some()
    }

    /**
     * Types of faces according to their orientation.
     */
    enum class FaceType {
        TOP,
        SIDE,
        BASE,
        NONE
    }

    /**
     * Returns the [FaceType] according to the [Polygon3D]'s orientation.
     */
    private fun Polygon3D.getType(): FaceType {
        val angleToZ = getNormal().getOrElse { throw it }.angle(Vector3D.Z_AXIS)

        return when {
            angleToZ < QUARTER_PI -> FaceType.TOP
            angleToZ in QUARTER_PI..THREE_QUARTER_PI -> FaceType.SIDE
            THREE_QUARTER_PI < angleToZ -> FaceType.BASE
            else -> FaceType.NONE
        }
    }

    /**
     * Returns either a cutout of a solid geometry (if it exists) or a [MultiSurfaceProperty] itself (if it exists).
     *
     * @param solidFaceSelection list of [FaceType] to be cutout of a solid geometry
     * @return cutout of a solid geometry or a [MultiSurfaceProperty]
     */
    fun getSolidCutoutOrSurface(vararg solidFaceSelection: FaceType): Option<Either<GeometryException.BoundaryRepresentationGenerationError, MultiSurfaceProperty>> {
        getSolidCutout(*solidFaceSelection).onSome {
            return it.right().some()
        }

        getMultiSurface().onSome {
            return it.some()
        }

        return None
    }

    /**
     * Returns a single [MultiSurfaceProperty] constructed of a solid's polygons which have been filtered by [FaceType].
     *
     * @param faceSelection list of relevant [FaceType]
     * @return [MultiSurfaceProperty] constructed of selected polygons from a solid geometry
     */
    fun getSolidCutout(vararg faceSelection: FaceType): Option<MultiSurfaceProperty> {
        val filteredPolygons = getFilteredPolygonsOfSolid(*faceSelection).handleEmpty { return None }
        return polygonsToMultiSurfaceProperty(filteredPolygons).some()
    }

    /**
     * Returns a list of individual [MultiSurfaceProperty], whereas each is based on solid's polygon filtered by
     * [FaceType].
     *
     * @param faceSelection list of relevant [FaceType]
     * @return list of [MultiSurfaceProperty], each constructed from an individual polygon of the solid geometry
     */
    fun getIndividualSolidCutouts(vararg faceSelection: FaceType): Option<NonEmptyList<MultiSurfaceProperty>> {
        val filteredPolygons: NonEmptyList<Polygon3D> =
            getFilteredPolygonsOfSolid(*faceSelection).handleEmpty { return None }

        return filteredPolygons.map { polygonsToMultiSurfaceProperty(nonEmptyListOf(it)) }.some()
    }

    private fun getFilteredPolygonsOfSolid(vararg faceSelection: FaceType): Option<NonEmptyList<Polygon3D>> {
        val polygonsOfSolid = polygonsOfSolidResult.handleEmpty { return None }

        return polygonsOfSolid
            .filter { polygon -> faceSelection.any { it == polygon.getType() } }
            .toNonEmptyListOrNone()
    }

    override fun visit(vector3D: Vector3D) {
        pointResult = vector3D.calculatePointGlobalCS().some()
        visit(vector3D as AbstractGeometry3D)
    }

    override fun visit(abstractCurve3D: AbstractCurve3D) {
        multiCurveResult = abstractCurve3D.calculateLineStringGlobalCS(parameters.discretizationStepSize).some()
    }

    override fun visit(abstractSurface3D: AbstractSurface3D) {
        polygonsOfSurfaceResult = abstractSurface3D.calculatePolygonsGlobalCS().some()
        visit(abstractSurface3D as AbstractGeometry3D)
    }

    override fun visit(circle3D: Circle3D) {
        val adjustedCircle = circle3D.copy(numberSlices = parameters.circleSlices)
        visit(adjustedCircle as AbstractSurface3D)
    }

    override fun visit(parametricBoundedSurface3D: ParametricBoundedSurface3D) {
        val adjustedParametricBoundedSurface =
            parametricBoundedSurface3D.copy(discretizationStepSize = parameters.sweepDiscretizationStepSize)
        visit(adjustedParametricBoundedSurface as AbstractSurface3D)
    }

    override fun visit(abstractSolid3D: AbstractSolid3D) {
        polygonsOfSolidResult = abstractSolid3D.calculatePolygonsGlobalCS().some()
        visit(abstractSolid3D as AbstractGeometry3D)
    }

    override fun visit(cylinder3D: Cylinder3D) {
        this.height = cylinder3D.height.some()
        this.diameter = cylinder3D.diameter.some()
        val adjustedCylinder = cylinder3D.copy(numberSlices = parameters.circleSlices)
        visit(adjustedCylinder as AbstractSolid3D)
    }

    override fun visit(parametricSweep3D: ParametricSweep3D) {
        val adjustedParametricSweep = parametricSweep3D
            .copy(discretizationStepSize = parameters.sweepDiscretizationStepSize)
        visit(adjustedParametricSweep as AbstractSolid3D)
    }

    override fun visit(abstractGeometry3D: AbstractGeometry3D) {
        this.rotation = abstractGeometry3D.affineSequence.solve().extractRotation().some()
    }

    private fun polygonsToMultiSurfaceProperty(polygons: NonEmptyList<Polygon3D>): MultiSurfaceProperty {
        val surfaceProperties = polygons.map {
            val gmlPolygon = geometryFactory.createPolygon(it.toVertexPositionElementList(), DIMENSION)!!
            if (parameters.generateRandomGeometryIds) gmlPolygon.id = generateRandomUUID(parameters.gmlIdPrefix)
            SurfaceProperty(gmlPolygon)
        }

        val multiSurface = MultiSurface(surfaceProperties)
        if (parameters.generateRandomGeometryIds) multiSurface.id = generateRandomUUID(parameters.gmlIdPrefix)
        return MultiSurfaceProperty(multiSurface)
    }

    private fun createDirectPosition(vector3D: Vector3D): DirectPosition =
        geometryFactory.createDirectPosition(vector3D.toDoubleArray(), DIMENSION)!!

    companion object {
        private val geometryFactory = GeometryFactory.newInstance()
        private const val DIMENSION = 3

        fun of(point: AbstractPoint3D, parameters: Roadspaces2CitygmlParameters): GeometryTransformer {
            return GeometryTransformer(parameters).also { point.accept(it) }
        }

        fun of(point: AbstractGeometry3D, parameters: Roadspaces2CitygmlParameters): GeometryTransformer {
            return GeometryTransformer(parameters).also { point.accept(it) }
        }
    }
}
