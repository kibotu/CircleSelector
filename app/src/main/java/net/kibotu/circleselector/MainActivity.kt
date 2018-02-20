package net.kibotu.circleselector

import android.animation.Animator
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.KITKAT
import android.os.Bundle
import android.support.v4.math.MathUtils.clamp
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.ViewConfiguration
import android.view.WindowManager
import com.dtx12.android_animations_actions.actions.Actions
import com.dtx12.android_animations_actions.actions.Actions.play
import com.dtx12.android_animations_actions.actions.Actions.scaleTo
import com.dtx12.android_animations_actions.actions.Interpolations
import kotlinx.android.synthetic.main.activity_main.*
import net.kibotu.circleselector.CircleSelector.Companion.nearestNumber
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val TIMER_10H_ANGLE = 322f
    private val SMART_MODE_ANGLE = 360f
    private val TIMER_15H_ANGLE = 397f
    private var currentSnapAngle: Float = 0f

    /**
     * long press duration in ms
     */
    private val longPressAnimationDuration = 200

    private val debugLinesEnabled = false

    private var nextSnapAngle = 0f

    private val animators: MutableList<Animator> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableImmersiveMode()

        CircleSelector.activity = this

        setContentView(R.layout.activity_main)

        play(scaleTo(0.5f, 0.5f), wheel)

        // set offset if necessary
        // hitBox.setPivotOffset(0, dpToPx(-12));

        hitBox.onTapListener = this::onTap

        hitBox.setLongPressTimeout(ViewConfiguration.getLongPressTimeout() - longPressAnimationDuration)

        hitBox.onLongPressedListener = this::onLongPressed

        hitBox.angleUpdateListener = this::onAngleUpdate
    }

    private fun onAngleUpdate(angle: Double, circleSelector: CircleSelector) {
        // Logger.v(tag(), "angle=" + angle.floatValue());

        // 0) don't do anything unless user is long pressing

        // 1) clamp angle
        val clampedAngle = clamp(circleSelector.angle.toFloat(), TIMER_10H_ANGLE, TIMER_15H_ANGLE)

        // 2) find nearest target angle
        nextSnapAngle = nearestNumber(clampedAngle, TIMER_10H_ANGLE, SMART_MODE_ANGLE, TIMER_15H_ANGLE)


        // 3) rotate between TIMER_10H_ANGLE and TIMER_15H_ANGLE
        if (nextSnapAngle != currentSnapAngle) {
            val animator = Actions.rotateTo(nextSnapAngle, 0.4f, Interpolations.BackEaseOut)
            animators.add(animator)
            play(animator, wheel)
        }

        currentSnapAngle = nextSnapAngle

        // 4) visually select timer10h / timer15h
        if (java.lang.Float.compare(currentSnapAngle, TIMER_10H_ANGLE) == 0) {
            // todo
        } else if (java.lang.Float.compare(currentSnapAngle, SMART_MODE_ANGLE) == 0) {
            // todo
        } else if (java.lang.Float.compare(currentSnapAngle, TIMER_15H_ANGLE) == 0) {
            // todo
        }

        // optional) debug lines
        if (!debugLinesEnabled)
            lines.visibility = GONE
        if (debugLinesEnabled)
            lines.setLines(Line(hitBox.center, hitBox.current))
    }

    private fun onLongPressed(isLongPressing: Boolean, circleSelector: CircleSelector) {
        Log.v(TAG, "[longPress] isLongPressing=" + isLongPressing)

        val duration = longPressAnimationDuration / 1000f

        // 1) scale wheel
        val scale = if (isLongPressing) 1f else 0.5f
        val animator = scaleTo(scale, scale, duration, if (isLongPressing) Interpolations.BackEaseOut else Interpolations.BackEaseIn)
        animators.add(animator)
        play(animator, wheel)

        // 3) send new mode if it has changed
        if (isLongPressing)
            return

        Log.v(TAG, "[longPress] isLongPressing=" + isLongPressing)

        if (java.lang.Float.compare(currentSnapAngle, TIMER_10H_ANGLE.toFloat()) == 0) {
            // todo
        } else if (java.lang.Float.compare(currentSnapAngle, SMART_MODE_ANGLE.toFloat()) == 0) {
            // todo
        } else if (java.lang.Float.compare(currentSnapAngle, TIMER_15H_ANGLE.toFloat()) == 0) {
            // todo
        }
    }

    internal var lastTap = System.currentTimeMillis()

    private fun onTap(duration: Int?, circleSelector: CircleSelector) {
        val currentTime = System.currentTimeMillis()
        Log.v(TAG, "[onTap] duration=" + duration + " lastTap=" + lastTap + " currentTime=" + currentTime + " difference=" + (currentTime - lastTap) + " tapTimeOut=" + ViewConfiguration.getDoubleTapTimeout())
        if (currentTime - lastTap < ViewConfiguration.getDoubleTapTimeout()) {
            return
        }
        lastTap = currentTime

        Log.v(TAG, "[onTap]")
    }

    override fun onPause() {
        super.onPause()

        cancelAnimations()
    }

    private fun cancelAnimations() {
        Log.v(TAG, "[cancelAnimations] " + animators.size)
        for (animator in animators)
            animator.cancel()

        animators.clear()
    }

    private fun enableImmersiveMode() {
        if (SDK_INT < KITKAT)
            return

        val window = window
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.setOnSystemUiVisibilityChangeListener({ visibility ->
            if (visibility != 0)
                return@setOnSystemUiVisibilityChangeListener

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        })
    }
}
