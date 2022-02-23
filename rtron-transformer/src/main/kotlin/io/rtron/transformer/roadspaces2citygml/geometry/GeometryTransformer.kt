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

package io.rtron.transformer.roadspaces2citygml.geometry

import arrow.core.Either
import com.github.kittinunf.result.map
import com.github.kittinunf.result.success
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.curve.LineString3D
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
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.std.handleFailure
import io.rtron.std.handleSuccess
import io.rtron.std.toEither
import io.rtron.std.toResult
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.module.IdentifierAdder
import org.citygml4j.model.core.ImplicitGeometry
import org.citygml4j.model.core.ImplicitGeometryProperty
import org.citygml4j.util.geometry.GeometryFactory
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
 * @param configuration parameters for the geometry transformation, such as discretization step sizes
 */
class GeometryTransformer(
    val configuration: Roadspaces2CitygmlConfiguration
) : Geometry3DVisitor {

    // Properties and Initializers
    private val _identifierAdder = IdentifierAdder(configuration)

    private lateinit var polygonsOfSolidResult: Either<Exception, List<Polygon3D>>
    private lateinit var polygonsOfSurfaceResult: Either<Exception, List<Polygon3D>>
    private lateinit var multiCurveResult: Either<Exception, LineString3D>
    private lateinit var pointResult: Either<Exception, Vector3D>

    private lateinit var rotation: Rotation3D
    private var height: Double = Double.NaN
    private var diameter: Double = Double.NaN

    // Methods
    fun isSetSolid() = this::polygonsOfSolidResult.isInitialized
    fun isSetMultiSurface() = this::polygonsOfSurfaceResult.isInitialized
    fun isSetMultiCurve() = this::multiCurveResult.isInitialized
    fun isSetPoint() = this::pointResult.isInitialized
    fun isSetImplicitGeometry() = isSetPoint()

    fun isSetRotation() = this::rotation.isInitialized
    fun isSetHeight() = !height.isNaN()
    fun isSetDiameter() = !diameter.isNaN()

    /**
     * Returns the result of the created solid geometry. If no solid geometry is available, an [IllegalStateException] is thrown.
     */
    fun getSolid(): Either<Exception, SolidProperty> {
        check(isSetSolid()) { "Solid geometry is not available." }
        val polygonsOfSolid = polygonsOfSolidResult.toResult().handleFailure { return Either.Left(it.error) }

        val gmlPolygons = polygonsOfSolid.map {
            val polygonGml = geometryFactory.createPolygon(it.toVertexPositionElementList(), DIMENSION)!!
            if (configuration.generateRandomGeometryIds) polygonGml.id = _identifierAdder.generateRandomUUID()
            SurfaceProperty(polygonGml)
        }

        val solid = Solid(Shell(gmlPolygons))
        if (configuration.generateRandomGeometryIds) solid.id = _identifierAdder.generateRandomUUID()

        val solidProperty = SolidProperty(solid)
        return Either.Right(solidProperty)
    }

    /**
     * Returns the result of the created multi surface geometry. If no multi surface geometry is available, an [IllegalStateException] is thrown.
     */
    fun getMultiSurface(): Either<Exception, MultiSurfaceProperty> {
        check(isSetMultiSurface()) { "MultiSurface geometry is not available." }
        val polygonsOfSurface = polygonsOfSurfaceResult.toResult().handleFailure { return it.toEither() }

        val multiSurfaceProperty = polygonsToMultiSurfaceProperty(polygonsOfSurface)
        return Either.Right(multiSurfaceProperty)
    }

    /**
     * Returns the result of the created multi curve geometry. If no multi curve geometry is available, an [IllegalStateException] is thrown.
     */
    fun getMultiCurve(): Either<Exception, MultiCurveProperty> {
        check(isSetMultiCurve()) { "MultiCurve geometry is not available." }
        val lineString = multiCurveResult.toResult().handleFailure { return it.toEither() }

        val coordinatesList = lineString.vertices.flatMap { it.toDoubleList() }
        val gmlLineString = geometryFactory.createLineString(coordinatesList, DIMENSION)!!
        val curveProperty = CurveProperty(gmlLineString)
        val multiCurve = MultiCurve(listOf(curveProperty))

        val multiCurveProperty = MultiCurveProperty(multiCurve)
        return Either.Right(multiCurveProperty)
    }

    /**
     * Returns the result of the created point geometry. If no point geometry is available, an [IllegalStateException] is thrown.
     */
    fun getPoint(): Either<Exception, PointProperty> {
        check(isSetPoint()) { "Point geometry is not available." }
        val point = pointResult.toResult().handleFailure { return it.toEither() }

        val gmlPoint = Point().apply {
            pos = createDirectPosition(point)
            if (configuration.generateRandomGeometryIds) id = _identifierAdder.generateRandomUUID()
        }
        val pointProperty = PointProperty(gmlPoint)
        return Either.Right(pointProperty)
    }

    /**
     * Returns the result of the rotation. If no rotation value is available, an [IllegalStateException] is thrown.
     */
    fun getRotation(): Either<IllegalStateException, Rotation3D> {
        check(isSetRotation()) { "Rotation is not available." }
        return Either.Right(rotation)
    }

    /**
     * Returns the result of the height value. If no height value is available, an [IllegalStateException] is thrown.
     */
    fun getHeight(): Either<IllegalStateException, Double> {
        check(isSetHeight()) { "Height is not available." }
        return Either.Right(height)
    }

    /**
     * Returns the result of the diameter value. If no diameter value is available, an [IllegalStateException] is thrown.
     */
    fun getDiameter(): Either<IllegalStateException, Double> {
        check(isSetDiameter()) { "Diameter is not available." }
        return Either.Right(diameter)
    }

    /**
     * Returns the result of the created implicit geometry. If no implicit geometry is available, an [IllegalStateException] is thrown.
     */
    fun getImplicitGeometry(): Either<Exception, ImplicitGeometryProperty> {
        check(isSetPoint()) { "ImplicitGeometry is not available." }

        val implicitGeometry = ImplicitGeometry().apply {
            referencePoint = getPoint().toResult().handleFailure { return it.toEither() }
            if (configuration.generateRandomGeometryIds) id = _identifierAdder.generateRandomUUID()
        }
        implicitGeometry.libraryObject = ""

        if (isSetRotation())
            getRotation().toResult().success { implicitGeometry.transformationMatrix = Affine3D.of(it).toGmlTransformationMatrix4x4() }

        val implicitGeometryProperty = ImplicitGeometryProperty(implicitGeometry)
        return Either.Right(implicitGeometryProperty)
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
        val angleToZ = getNormal().toResult().handleFailure { throw it.error }.angle(Vector3D.Z_AXIS)

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
    fun getSolidCutoutOrSurface(vararg solidFaceSelection: FaceType): Either<IllegalStateException, MultiSurfaceProperty> {
        if (isSetSolid())
            getSolidCutout(*solidFaceSelection).toResult().handleSuccess { return it.toEither() }
        if (isSetMultiSurface())
            getMultiSurface().toResult().handleSuccess { return it.toEither() }

        return Either.Left(IllegalStateException("No cutout of solid or MultiSurfaceProperty available for geometry."))
    }

    /**
     * Returns a single [MultiSurfaceProperty] constructed of a solid's polygons which have been filtered by [FaceType].
     *
     * @param faceSelection list of relevant [FaceType]
     * @return [MultiSurfaceProperty] constructed of selected polygons from a solid geometry
     */
    fun getSolidCutout(vararg faceSelection: FaceType): Either<Exception, MultiSurfaceProperty> {
        val filteredPolygons = getFilteredPolygonsOfSolid(*faceSelection).toResult().handleFailure { return it.toEither() }

        val multiSurfaceProperty = polygonsToMultiSurfaceProperty(filteredPolygons)
        return Either.Right(multiSurfaceProperty)
    }

    /**
     * Returns a list of individual [MultiSurfaceProperty], whereas each is based on solid's polygon filtered by
     * [FaceType].
     *
     * @param faceSelection list of relevant [FaceType]
     * @return list of [MultiSurfaceProperty], each constructed from an individual polygon of the solid geometry
     */
    fun getIndividualSolidCutouts(vararg faceSelection: FaceType): Either<Exception, List<MultiSurfaceProperty>> {
        val filteredPolygons = getFilteredPolygonsOfSolid(*faceSelection).toResult().handleFailure { return Either.Left(it.error) }

        return filteredPolygons
            .map { polygonsToMultiSurfaceProperty(listOf(it)) }
            .let { Either.Right(it) }
    }

    private fun getFilteredPolygonsOfSolid(vararg faceSelection: FaceType): Either<Exception, List<Polygon3D>> {
        if (!this::polygonsOfSolidResult.isInitialized)
            return Either.Left(IllegalStateException("No MultiSurfaceProperty available for geometry."))
        val polygonsOfSolid = polygonsOfSolidResult.toResult().handleFailure { return Either.Left(it.error) }

        val filteredPolygons = polygonsOfSolid.filter { polygon -> faceSelection.any { it == polygon.getType() } }
        if (filteredPolygons.isEmpty())
            return Either.Left(IllegalStateException("No polygons selected for constructing a MultiSurface from a solid geometry."))

        return Either.Right(filteredPolygons)
    }

    override fun visit(vector3D: Vector3D) {
        pointResult = Either.Right(vector3D.calculatePointGlobalCS())
        visit(vector3D as AbstractGeometry3D)
    }

    override fun visit(abstractCurve3D: AbstractCurve3D) {
        multiCurveResult = abstractCurve3D.calculateLineStringGlobalCS(configuration.discretizationStepSize)
    }

    override fun visit(abstractSurface3D: AbstractSurface3D) {
        polygonsOfSurfaceResult = abstractSurface3D.calculatePolygonsGlobalCS()
        visit(abstractSurface3D as AbstractGeometry3D)
    }

    override fun visit(circle3D: Circle3D) {
        val adjustedCircle = circle3D.copy(numberSlices = configuration.circleSlices)
        visit(adjustedCircle as AbstractSurface3D)
    }

    override fun visit(parametricBoundedSurface3D: ParametricBoundedSurface3D) {
        val adjustedParametricBoundedSurface = parametricBoundedSurface3D.copy(discretizationStepSize = configuration.sweepDiscretizationStepSize)
        visit(adjustedParametricBoundedSurface as AbstractSurface3D)
    }

    override fun visit(abstractSolid3D: AbstractSolid3D) {
        polygonsOfSolidResult = abstractSolid3D.calculatePolygonsGlobalCS()
        visit(abstractSolid3D as AbstractGeometry3D)
    }

    override fun visit(cylinder3D: Cylinder3D) {
        this.height = cylinder3D.height
        this.diameter = cylinder3D.diameter
        val adjustedCylinder = cylinder3D.copy(numberSlices = configuration.circleSlices)
        visit(adjustedCylinder as AbstractSolid3D)
    }

    override fun visit(parametricSweep3D: ParametricSweep3D) {
        val adjustedParametricSweep = parametricSweep3D
            .copy(discretizationStepSize = configuration.sweepDiscretizationStepSize)
        visit(adjustedParametricSweep as AbstractSolid3D)
    }

    override fun visit(abstractGeometry3D: AbstractGeometry3D) {
        this.rotation = abstractGeometry3D.affineSequence.solve().extractRotation()
    }

    private fun polygonsToMultiSurfaceProperty(polygons: List<Polygon3D>): MultiSurfaceProperty {
        require(polygons.isNotEmpty()) { "Must contain polygons." }

        val surfaceProperties = polygons.map {
            val gmlPolygon = geometryFactory.createPolygon(it.toVertexPositionElementList(), DIMENSION)!!
            if (configuration.generateRandomGeometryIds) gmlPolygon.id = _identifierAdder.generateRandomUUID()
            SurfaceProperty(gmlPolygon)
        }

        val multiSurface = MultiSurface(surfaceProperties)
        if (configuration.generateRandomGeometryIds) multiSurface.id = _identifierAdder.generateRandomUUID()
        return MultiSurfaceProperty(multiSurface)
    }

    private fun createDirectPosition(vector3D: Vector3D): DirectPosition =
        geometryFactory.createDirectPosition(vector3D.toDoubleArray(), DIMENSION)!!

    companion object {
        private val geometryFactory = GeometryFactory.newInstance()
        private const val DIMENSION = 3

        fun of(roadspaceObject: RoadspaceObject, configuration: Roadspaces2CitygmlConfiguration): GeometryTransformer {
            require(roadspaceObject.geometry.size == 1) { "Roadspace object (${roadspaceObject.id}) must contain exactly one geometrical representation." }
            val currentGeometricPrimitive = roadspaceObject.geometry.first()

            return GeometryTransformer(configuration).also { currentGeometricPrimitive.accept(it) }
        }
    }
}
