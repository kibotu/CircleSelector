package net.kibotu.circleselector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import java.lang.ref.WeakReference


/**
 * http://neevek.net/posts/2013/10/13/implementing-onInterceptTouchEvent-and-onTouchEvent-for-ViewGroup.html
 *
 *
 * Purpose of this view is to being able to communicate
 * <pre>
 * 0) consume all events of children and parents
 * 1) click
 * 2) long press
 * 3) dragging
 * 4) angle between pivot and latest touch event point
</pre> *
 */
open class CircleSelector @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var enableLogging = false

    private var previous = Vector2()
    private var start = Vector2()

    /**
     * Defines the time in millis when the long press event gets fired.
     */
    private var longPressTimeout: Int = 0

    /**
     * Distance after which it counts as move gesture.
     */
    private var minDistanceMovingGesture: Int = 0

    /**
     * Defines if view is currently in a long press gesture.
     */
    // endregion

    // region long press

    private var isLongPressing: Boolean = false

    /**
     * Defines if view is currently in active a moving gesture.
     */
    private var isBeingDragged: Boolean = false

    /**
     * Holds current angle between pivot and current touch point.
     */
    /**
     * Retrieves current angle between pivot of view and latest touch gesture point.
     *
     * @return Euler angle with offset [DEGREE_OFFSET]
     */
    var angle: Double = 0.toDouble()
        private set(angle) {
            onAngleUpdate(angle)
            field = angle
        }

    /**
     * Used for clamped Y value.
     */
    private var currentClamped = Vector2()

    /**
     * Holds current touch coordinate.
     */
    var current = Vector2()

    /**
     * Defines center of the screen.
     */
    private lateinit var screenCenter: Vector2

    /**
     * Defines pivot of view.
     */
    lateinit var center: Vector2

    /**
     * Defines pivot offset.
     */
    private var offset: Vector2 = Vector2()

    /**
     * <img src="http://i.imgur.com/Cwtr7ku.png"></img>
     *
     *
     * Returns angle calculated by the dragging movement based from the screenCenter of the screen.
     *
     * @return Angle in degree.
     */
    var angleUpdateListener: ((Double, CircleSelector) -> Unit)? = null

    /**
     * Callback for moving gesture changes.
     */
    var onDragUpdateListener: ((Boolean, CircleSelector) -> Unit)? = null

    /**
     * Callback for long press changes. [longPressTimeout]
     */
    var onLongPressedListener: ((Boolean, CircleSelector) -> Unit)? = null

    /**
     * Callback for click event.
     */
    var onTapListener: ((Int, CircleSelector) -> Unit)? = null

    /**
     * used as buffer to retrieve view location on screen
     */
    private var location = IntArray(2)

    private val isMoveGesture: Boolean
        get() = isMoveGesture(start, current, minDistanceMovingGesture.toFloat())

    init {

        if (!isInEditMode) {
            val screenDimensions = getScreenDimensions(CircleSelector.activity)
            screenCenter = Vector2(screenDimensions.width / 2f, screenDimensions.height / 2f)
        }

        longPressTimeout = ViewConfiguration.getLongPressTimeout()
        minDistanceMovingGesture = dpToPx(20)
    }

    /**
     * {@inheritDoc}
     *
     *
     * <pre>
     * In a nutshell: it can consume touch events before its children.
     *
     * 0) consume all events of children and parents
    </pre> *
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        if (isInEditMode)
            return false

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                log("[onInterceptTouchEvent] ACTION_DOWN " + event)
                previous.set(event.rawX, event.rawY)
                start.set(event.rawX, event.rawY)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                log("[onInterceptTouchEvent] ACTION_UP || ACTION_CANCEL downTime=" + (event.eventTime - event.downTime) + " " + event)
                seDragging(false)
            }

            MotionEvent.ACTION_MOVE -> seDragging(true)

            else -> log("[onInterceptTouchEvent] Unhandled " + event)
        }

        return true
    }

    /**
     * {@inheritDoc}
     *
     *
     * We communicate the following events to callbacks:
     * <pre>
     * 0) consume all events of children and parents
     * 1) click
     * 2) long press
     * 3) dragging
     * 4) angle between pivot and latest touch event point
    </pre> *
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (isInEditMode)
            return false

        // touch event duration
        val downTime = event.eventTime - event.downTime

        when (event.action) {

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                log("[onTouchEvent] ACTION_UP || ACTION_CANCEL downTime=$downTime $event")
                seDragging(false)

                // downtime below long press duration then fire click, otherwise send cancel long press event
                if (downTime < longPressTimeout) {
                    if (!isMoveGesture)
                        onClick(downTime.toInt())
                } else
                    onLongPress(false)
            }

            MotionEvent.ACTION_MOVE -> {

                log("[onTouchEvent] ACTION_MOVE downTime=$downTime $event")

                getLocationInWindow(location)
                current.set(event.rawX, event.rawY)
                currentClamped.set(event.rawX, location[1].toFloat())
                center = Vector2(location[0].toFloat() + width / 2f + offset.x, location[1].toFloat() + height / 2f + offset.y)
                angle = Vector2(currentClamped).sub(center).nor().angle() + DEGREE_OFFSET

                if (downTime >= longPressTimeout) {
                    log("[ACTION_MOVE] downTime=" + downTime)
                    onLongPress(true)
                }

                val consume = !isMoveGesture || isLongPressing
                log("[isMoveGesture] start=" + start + " current=" + current + " distance=" + start.dst(current) + " minDistanceMovingGesture=" + minDistanceMovingGesture + " isMoveGesture=" + isMoveGesture + " isLongPressing=" + isLongPressing + " consume=" + consume)

                // consume touch event inside scroll container like view pagers
                parent.requestDisallowInterceptTouchEvent(consume)

                seDragging(true)
            }

            else -> log("[onTouchEvent] Unhandled " + event)
        }

        return true
    }

    // region click

    private fun onClick(duration: Int) {
        log("[onClick] duration=$duration")
        onTapListener?.invoke(duration, this)
    }

    private fun onLongPress(isLongPressing: Boolean) {
        if (this.isLongPressing != isLongPressing)
            onLongPressedListener?.invoke(isLongPressing, this)

        this.isLongPressing = isLongPressing
    }

    fun setLongPressTimeout(longPressTimeout: Int) {
        this.longPressTimeout = Math.abs(longPressTimeout)
    }

    // endregion

    // region angle


    private fun onAngleUpdate(angle: Double) {
        if (java.lang.Double.compare(this.angle, angle) != 0)
            angleUpdateListener?.invoke(angle, this)
    }

    /**
     * Changes Pivot for computing angle.
     */
    fun setPivotOffset(offsetX: Int, offsetY: Int) {
        offset.set(offsetX.toFloat(), offsetY.toFloat())
        postInvalidate()
    }

    // endregion

    // region dragging

    /**
     * Fires moving gesture event.
     *
     * @param isBeingDragged `true` if being dragged.
     */
    private fun seDragging(isBeingDragged: Boolean) {
        onDragUpdate(isBeingDragged)
        this.isBeingDragged = isBeingDragged
    }

    private fun onDragUpdate(isBeingDragged: Boolean) {
        if (this.isBeingDragged != isBeingDragged)
            onDragUpdateListener?.invoke(isLongPressing, this)
    }

    // endregion

    private fun tag(): String {
        return javaClass.simpleName
    }

    private fun log(message: String?) {
        if (enableLogging)
            Log.v(tag(), message)
    }

    companion object {

        private var _activity: WeakReference<Activity?>? = null

        @JvmStatic
        var activity: Activity?
            set(value) {
                _activity = WeakReference(value)
            }
            get() = _activity?.get()

        /**
         * <img src="http://i.imgur.com/Cwtr7ku.png"></img>
         *
         *
         * We need a 90 degree offset towards [Math.atan2].
         */
        const val DEGREE_OFFSET = 90.0

        fun dpToPx(dp: Int): Int {
            val scale = density
            return (dp * scale + 0.5f).toInt()
        }

        private val density: Float
            get() = Resources.getSystem().displayMetrics.density

        private fun getDefaultDisplay(activity: Activity?): android.view.Display {
            return activity!!.windowManager.defaultDisplay
        }

        fun getScreenDimensions(activity: Activity?): Dimension {

            val dm = DisplayMetrics()
            val display = getDefaultDisplay(activity)
            display.getMetrics(dm)

            var screenWidth = dm.widthPixels
            var screenHeight = dm.heightPixels

            if (Build.VERSION.SDK_INT < 17) {
                try {
                    screenWidth = android.view.Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                    screenHeight = android.view.Display::class.java.getMethod("getRawHeight").invoke(display) as Int
                } catch (ignored: Exception) {
                }

            }
            if (Build.VERSION.SDK_INT >= 17) {
                try {
                    val realSize = Point()
                    android.view.Display::class.java.getMethod("getRealSize", Point::class.java).invoke(display, realSize)
                    screenWidth = realSize.x
                    screenHeight = realSize.y
                } catch (ignored: Exception) {
                }

            }

            return Dimension(screenWidth, screenHeight)
        }

        private fun isMoveGesture(start: Vector2?, end: Vector2?, distance: Float): Boolean {
            return start != null && end != null && start.dst(end) >= distance
        }


        fun nearestNumber(number: Float, vararg numbers: Float): Float {
            var distance = Math.abs(numbers[0] - number)
            var index = 0
            for (c in 1 until numbers.size) {
                val cDistance = Math.abs(numbers[c] - number)
                if (cDistance < distance) {
                    index = c
                    distance = cDistance
                }
            }
            return numbers[index]
        }
    }
}
