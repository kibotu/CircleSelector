package net.kibotu.circleselector

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable
import java.lang.Float.floatToIntBits


/**
 * Encapsulates a 2D vector. Allows chaining methods by returning a reference to itself
 *
 *
 * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Vector2.java
 *
 * @author badlogicgames@gmail.com
 */
class Vector2 : Serializable {

    /**
     * the x-component of this vector
     */
    var x: Float = 0.toFloat()
    /**
     * the y-component of this vector
     */
    var y: Float = 0.toFloat()


    val isUnit: Boolean
        get() = isUnit(0.000000001f)


    val isZero: Boolean
        get() = x == 0f && y == 0f

    /**
     * Constructs a new vector at (0,0)
     */
    constructor()

    /**
     * Constructs a vector with the given components
     *
     * @param x The x-component
     * @param y The y-component
     */
    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /**
     * Constructs a vector from the given vector
     *
     * @param v The vector
     */
    constructor(v: Vector2) {
        set(v)
    }

    fun cpy(): Vector2 {
        return Vector2(this)
    }

    fun len(): Float {
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    fun len2(): Float {
        return x * x + y * y
    }

    fun set(v: Vector2): Vector2 {
        x = v.x
        y = v.y
        return this
    }

    /**
     * Sets the components of this vector
     *
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    operator fun set(x: Float, y: Float): Vector2 {
        this.x = x
        this.y = y
        return this
    }

    fun sub(v: Vector2): Vector2 {
        x -= v.x
        y -= v.y
        return this
    }

    /**
     * Substracts the other vector from this vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return This vector for chaining
     */
    fun sub(x: Float, y: Float): Vector2 {
        this.x -= x
        this.y -= y
        return this
    }

    fun nor(): Vector2 {
        val len = len()
        if (len != 0f) {
            x /= len
            y /= len
        }
        return this
    }

    fun add(v: Vector2): Vector2 {
        x += v.x
        y += v.y
        return this
    }

    /**
     * Adds the given components to this vector
     *
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    fun add(x: Float, y: Float): Vector2 {
        this.x += x
        this.y += y
        return this
    }

    fun dot(v: Vector2): Float {
        return x * v.x + y * v.y
    }

    fun dot(ox: Float, oy: Float): Float {
        return x * ox + y * oy
    }

    fun scl(scalar: Float): Vector2 {
        x *= scalar
        y *= scalar
        return this
    }

    /**
     * Multiplies this vector by a scalar
     *
     * @return This vector for chaining
     */
    fun scl(x: Float, y: Float): Vector2 {
        this.x *= x
        this.y *= y
        return this
    }

    fun scl(v: Vector2): Vector2 {
        this.x *= v.x
        this.y *= v.y
        return this
    }

    fun mulAdd(vec: Vector2, scalar: Float): Vector2 {
        this.x += vec.x * scalar
        this.y += vec.y * scalar
        return this
    }

    fun mulAdd(vec: Vector2, mulVec: Vector2): Vector2 {
        this.x += vec.x * mulVec.x
        this.y += vec.y * mulVec.y
        return this
    }


    fun dst(v: Vector2): Float {
        val x_d = v.x - x
        val y_d = v.y - y
        return Math.sqrt((x_d * x_d + y_d * y_d).toDouble()).toFloat()
    }

    /**
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return the distance between this and the other vector
     */
    fun dst(x: Float, y: Float): Float {
        val x_d = x - this.x
        val y_d = y - this.y
        return Math.sqrt((x_d * x_d + y_d * y_d).toDouble()).toFloat()
    }

    fun dst2(v: Vector2): Float {
        val x_d = v.x - x
        val y_d = v.y - y
        return x_d * x_d + y_d * y_d
    }

    /**
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return the squared distance between this and the other vector
     */
    fun dst2(x: Float, y: Float): Float {
        val x_d = x - this.x
        val y_d = y - this.y
        return x_d * x_d + y_d * y_d
    }

    fun limit(limit: Float): Vector2 {
        return limit2(limit * limit)
    }

    fun limit2(limit2: Float): Vector2 {
        val len2 = len2()
        return if (len2 > limit2) {
            scl(Math.sqrt((limit2 / len2).toDouble()).toFloat())
        } else this
    }

    fun clamp(min: Float, max: Float): Vector2 {
        val len2 = len2()
        if (len2 == 0f) return this
        val max2 = max * max
        if (len2 > max2) return scl(Math.sqrt((max2 / len2).toDouble()).toFloat())
        val min2 = min * min
        return if (len2 < min2) scl(Math.sqrt((min2 / len2).toDouble()).toFloat()) else this
    }

    fun setLength(len: Float): Vector2 {
        return setLength2(len * len)
    }

    fun setLength2(len2: Float): Vector2 {
        val oldLen2 = len2()
        return if (oldLen2 == 0f || oldLen2 == len2) this else scl(Math.sqrt((len2 / oldLen2).toDouble()).toFloat())
    }

    /**
     * Converts this `Vector2` to a string in the format `(x,y)`.
     *
     * @return a string representation of this object.
     */
    override fun toString(): String {
        return "($x,$y)"
    }

    /**
     * Calculates the 2D cross product between this and the given vector.
     *
     * @param v the other vector
     * @return the cross product
     */
    fun crs(v: Vector2): Float {
        return this.x * v.y - this.y * v.x
    }

    /**
     * Calculates the 2D cross product between this and the given vector.
     *
     * @param x the x-coordinate of the other vector
     * @param y the y-coordinate of the other vector
     * @return the cross product
     */
    fun crs(x: Float, y: Float): Float {
        return this.x * y - this.y * x
    }

    /**
     * @return the angle in degrees of this vector (point) relative to the x-axis. Angles are towards the positive y-axis (typically
     * counter-clockwise) and between 0 and 360.
     */
    fun angle(): Float {
        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()
        if (angle < 0) angle += 360f
        return angle
    }

    /**
     * @return the angle in degrees of this vector (point) relative to the given vector. Angles are towards the positive y-axis
     * (typically counter-clockwise.) between -180 and +180
     */
    fun angle(reference: Vector2): Float {
        return Math.toDegrees(Math.atan2(crs(reference).toDouble(), dot(reference).toDouble())).toFloat()
    }

    /**
     * @return the angle in radians of this vector (point) relative to the x-axis. Angles are towards the positive y-axis.
     * (typically counter-clockwise)
     */
    fun angleRad(): Float {
        return Math.atan2(y.toDouble(), x.toDouble()).toFloat()
    }

    /**
     * @return the angle in radians of this vector (point) relative to the given vector. Angles are towards the positive y-axis.
     * (typically counter-clockwise.)
     */
    fun angleRad(reference: Vector2): Float {
        return Math.atan2(crs(reference).toDouble(), dot(reference).toDouble()).toFloat()
    }

    /**
     * Sets the angle of the vector in degrees relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
     *
     * @param degrees The angle in degrees to set.
     */
    fun setAngle(degrees: Float): Vector2 {
        return setAngleRad(Math.toRadians(degrees.toDouble()).toFloat())
    }

    /**
     * Sets the angle of the vector in radians relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
     *
     * @param radians The angle in radians to set.
     */
    fun setAngleRad(radians: Float): Vector2 {
        this[len()] = 0f
        this.rotateRad(radians)

        return this
    }

    /**
     * Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
     *
     * @param degrees the angle in degrees
     */
    fun rotate(degrees: Float): Vector2 {
        return rotateRad(Math.toRadians(degrees.toDouble()).toFloat())
    }

    /**
     * Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
     *
     * @param radians the angle in radians
     */
    fun rotateRad(radians: Float): Vector2 {
        val cos = Math.cos(radians.toDouble()).toFloat()
        val sin = Math.sin(radians.toDouble()).toFloat()

        val newX = this.x * cos - this.y * sin
        val newY = this.x * sin + this.y * cos

        this.x = newX
        this.y = newY

        return this
    }

    /**
     * Rotates the Vector2 by 90 degrees in the specified direction, where >= 0 is counter-clockwise and < 0 is clockwise.
     */
    fun rotate90(dir: Int): Vector2 {
        val x = this.x
        if (dir >= 0) {
            this.x = -y
            y = x
        } else {
            this.x = y
            y = -x
        }
        return this
    }


    fun lerp(target: Vector2, alpha: Float): Vector2 {
        val invAlpha = 1.0f - alpha
        this.x = x * invAlpha + target.x * alpha
        this.y = y * invAlpha + target.y * alpha
        return this
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + floatToIntBits(x)
        result = prime * result + floatToIntBits(y)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        val other = other as Vector2?
        if (floatToIntBits(x) != floatToIntBits(other!!.x)) return false
        return floatToIntBits(y) == floatToIntBits(other.y)
    }


    fun epsilonEquals(other: Vector2?, epsilon: Float): Boolean {
        if (other == null) return false
        if (Math.abs(other.x - x) > epsilon) return false
        return Math.abs(other.y - y) <= epsilon
    }

    /**
     * Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
     *
     * @return whether the vectors are the same.
     */
    fun epsilonEquals(x: Float, y: Float, epsilon: Float): Boolean {
        if (Math.abs(x - this.x) > epsilon) return false
        return Math.abs(y - this.y) <= epsilon
    }


    fun isUnit(margin: Float): Boolean {
        return Math.abs(len2() - 1f) < margin
    }


    fun isZero(margin: Float): Boolean {
        return len2() < margin
    }


    fun isOnLine(other: Vector2): Boolean {
        return isZero(x * other.y - y * other.x)
    }

    fun isOnLine(other: Vector2, epsilon: Float): Boolean {
        return isZero(x * other.y - y * other.x, epsilon)
    }


    fun isCollinear(other: Vector2, epsilon: Float): Boolean {
        return isOnLine(other, epsilon) && dot(other) > 0f
    }


    fun isCollinear(other: Vector2): Boolean {
        return isOnLine(other) && dot(other) > 0f
    }


    fun isCollinearOpposite(other: Vector2, epsilon: Float): Boolean {
        return isOnLine(other, epsilon) && dot(other) < 0f
    }


    fun isCollinearOpposite(other: Vector2): Boolean {
        return isOnLine(other) && dot(other) < 0f
    }


    fun isPerpendicular(vector: Vector2): Boolean {
        return isZero(dot(vector))
    }


    fun isPerpendicular(vector: Vector2, epsilon: Float): Boolean {
        return isZero(dot(vector), epsilon)
    }


    fun hasSameDirection(vector: Vector2): Boolean {
        return dot(vector) > 0
    }


    fun hasOppositeDirection(vector: Vector2): Boolean {
        return dot(vector) < 0
    }


    fun setZero(): Vector2 {
        this.x = 0f
        this.y = 0f
        return this
    }

    companion object {
        private const val serialVersionUID = 913902788239530931L

        val X = Vector2(1f, 0f)
        val Y = Vector2(0f, 1f)
        val Zero = Vector2(0f, 0f)

        fun len(x: Float, y: Float): Float {
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }

        fun len2(x: Float, y: Float): Float {
            return x * x + y * y
        }

        fun dot(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            return x1 * x2 + y1 * y2
        }

        fun dst(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val x_d = x2 - x1
            val y_d = y2 - y1
            return Math.sqrt((x_d * x_d + y_d * y_d).toDouble()).toFloat()
        }

        fun dst2(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val x_d = x2 - x1
            val y_d = y2 - y1
            return x_d * x_d + y_d * y_d
        }

        /**
         * Returns true if the value is zero.
         *
         * @param tolerance represent an upper bound below which the value is considered zero.
         */
        fun isZero(value: Float, tolerance: Float): Boolean {
            return Math.abs(value) <= tolerance
        }
    }
}
