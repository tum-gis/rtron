package io.rtron.math.processing

import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Vector2DListExtensionsTest {

    @Nested
    inner class TestVectorsAreClockwiseOrdered {

        @Test
        fun `basic three-vertices-triangle should have a clockwise order`() {
            val pointA = Vector2D.ZERO
            val pointB = Vector2D.Y_AXIS
            val pointC = Vector2D.X_AXIS
            val vertices = listOf(pointA, pointB, pointC)

            val hasClockwiseOrder = vertices.isClockwiseOrdered()

            assertTrue(hasClockwiseOrder)
        }

        @Test
        fun `basic three-vertices-triangle should not have a clockwise order`() {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D.Y_AXIS
            val pointC = Vector2D.ZERO
            val vertices = listOf(pointA, pointB, pointC)

            val hasClockwiseOrder = vertices.isClockwiseOrdered()

            assertFalse(hasClockwiseOrder)
        }

        @Test
        fun `five-vertices-triangle should not have a clockwise order`() {
            val pointA = Vector2D(5.0, 0.0)
            val pointB = Vector2D(6.0, 4.0)
            val pointC = Vector2D(4.0, 5.0)
            val pointD = Vector2D(1.0, 5.0)
            val pointE = Vector2D(1.0, 0.0)
            val vertices = listOf(pointA, pointB, pointC, pointD, pointE)

            val hasClockwiseOrder = vertices.isClockwiseOrdered()

            assertFalse(hasClockwiseOrder)
        }

        @Test
        fun `providing two vertices should fail`() {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D.Y_AXIS
            val vertices = listOf(pointA, pointB)

            Assertions.assertThatIllegalArgumentException().isThrownBy {
                vertices.isClockwiseOrdered()
            }
        }
    }
}
