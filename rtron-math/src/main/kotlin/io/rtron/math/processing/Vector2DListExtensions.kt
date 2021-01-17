package io.rtron.math.processing

import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.std.zipWithNextEnclosing

/**
 * Returns true, if the list of [Vector2D] has a clockwise order.
 *
 * @see [StackOverflow Answer](https://stackoverflow.com/a/1165943)
 */
fun List<Vector2D>.isClockwiseOrdered(): Boolean {
    require(this.distinct().size >= 3) { "At least three distinct vectors must be provided." }
    return zipWithNextEnclosing().map { (it.second.x - it.first.x) * (it.second.y + it.first.y) }.sum() > 0.0
}

/**
 * Returns true, if the list of [Vector2D] has an anti-clockwise order.
 */
fun List<Vector2D>.isCounterClockwiseOrdered(): Boolean = !this.isClockwiseOrdered()
