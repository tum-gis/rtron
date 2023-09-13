package io.rtron.math.processing

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.rtron.math.geometry.euclidean.twod.point.Vector2D

class Vector2DListExtensionsTest : FunSpec({
    context("TestVectorsAreClockwiseOrdered") {

        test("basic three-vertices-triangle should have a clockwise order") {
            val pointA = Vector2D.ZERO
            val pointB = Vector2D.Y_AXIS
            val pointC = Vector2D.X_AXIS
            val vertices = listOf(pointA, pointB, pointC)

            val hasClockwiseOrder = vertices.isClockwiseOrdered()

            hasClockwiseOrder.shouldBeTrue()
        }

        test("basic three-vertices-triangle should not have a clockwise order") {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D.Y_AXIS
            val pointC = Vector2D.ZERO
            val vertices = listOf(pointA, pointB, pointC)

            val hasClockwiseOrder = vertices.isClockwiseOrdered()

            hasClockwiseOrder.shouldBeFalse()
        }

        test("five-vertices-triangle should not have a clockwise order") {
            val pointA = Vector2D(5.0, 0.0)
            val pointB = Vector2D(6.0, 4.0)
            val pointC = Vector2D(4.0, 5.0)
            val pointD = Vector2D(1.0, 5.0)
            val pointE = Vector2D(1.0, 0.0)
            val vertices = listOf(pointA, pointB, pointC, pointD, pointE)

            val hasClockwiseOrder = vertices.isClockwiseOrdered()

            hasClockwiseOrder.shouldBeFalse()
        }

        test("providing two vertices should fail") {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D.Y_AXIS
            val vertices = listOf(pointA, pointB)

            shouldThrow<IllegalArgumentException> {
                vertices.isClockwiseOrdered()
            }
        }
    }
})
