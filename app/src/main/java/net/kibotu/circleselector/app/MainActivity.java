package net.kibotu.circleselector.app;

import android.animation.Animator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.common.android.utils.misc.Line;
import com.common.android.utils.ui.LinesView;
import com.dtx12.android_animations_actions.actions.Actions;
import com.dtx12.android_animations_actions.actions.Interpolations;

import net.kibotu.circleselector.TouchViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.common.android.utils.extensions.MathExtensions.clamp;
import static com.common.android.utils.extensions.MathExtensions.nearestNumber;
import static com.common.android.utils.extensions.ViewExtensions.hideViewsCompletely;
import static com.dtx12.android_animations_actions.actions.Actions.play;
import static com.dtx12.android_animations_actions.actions.Actions.scaleTo;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    LinesView linesView;
    TouchViewGroup hitBox;
    ImageView wheel;

    private static final int TIMER_10H_ANGLE = 322;
    private static final int SMART_MODE_ANGLE = 360;
    private static final int TIMER_15H_ANGLE = 397;
    private float currentSnapAngle;

    List<Animator> animators = new ArrayList<>();

    /**
     * long press duration in ms
     */
    int longPressAnimationDuration = 200;

    boolean debugLinesEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // debug lines
        linesView = (LinesView) findViewById(R.id.lines);
        hitBox = (TouchViewGroup) findViewById(R.id.hitbox);
        wheel = (ImageView) findViewById(R.id.wheel);

        play(scaleTo(0.5f, 0.5f), wheel);

        // set offset if necessary
        // hitBox.setPivotOffset(0, dpToPx(-12));

        hitBox.setOnTapListener(this::onTap);

        hitBox.setLongPressTimeout(ViewConfiguration.getLongPressTimeout() - longPressAnimationDuration);
        hitBox.setOnLongPressedListener(this::onLongPressed);

        hitBox.setAngleUpdateListener(this::onAngleUpdate);

    }

    float nextSnapAngle = 0;

    private void onAngleUpdate(double angle, TouchViewGroup touchViewGroup) {
        // Logger.v(tag(), "angle=" + angle.floatValue());

        // 0) don't do anything unless user is long pressing

        // 1) clamp angle
        float clampedAngle = clamp((float) touchViewGroup.getAngle(), TIMER_10H_ANGLE, TIMER_15H_ANGLE);

        // 2) find nearest target angle
        nextSnapAngle = nearestNumber(clampedAngle, TIMER_10H_ANGLE, SMART_MODE_ANGLE, TIMER_15H_ANGLE);


        // 3) rotate between TIMER_10H_ANGLE and TIMER_15H_ANGLE
        if (nextSnapAngle != currentSnapAngle) {
            Animator animator = Actions.rotateTo(nextSnapAngle, 0.4f, Interpolations.BackEaseOut);
            animators.add(animator);
            play(animator, wheel);
        }

        currentSnapAngle = nextSnapAngle;

        // 4) visually select timer10h / timer15h
        if (Float.compare(currentSnapAngle, TIMER_10H_ANGLE) == 0) {
            // todo
        } else if (Float.compare(currentSnapAngle, SMART_MODE_ANGLE) == 0) {
            // todo
        } else if (Float.compare(currentSnapAngle, TIMER_15H_ANGLE) == 0) {
            // todo
        }

        // optional) debug lines
        hideViewsCompletely(!debugLinesEnabled, linesView);
        if (debugLinesEnabled)
            linesView.setLines(new Line(hitBox.center, hitBox.current));
    }

    private void onLongPressed(boolean isLongPressing, TouchViewGroup touchViewGroup) {
        Log.v(TAG, "[longPress] isLongPressing=" + isLongPressing);

        final float duration = longPressAnimationDuration / 1000f;

        // 1) scale wheel
        float scale = isLongPressing ? 1f : 0.5f;
        Animator animator = scaleTo(scale, scale, duration, isLongPressing ? Interpolations.BackEaseOut : Interpolations.BackEaseIn);
        animators.add(animator);
        play(animator, wheel);

        // 3) send new mode if it has changed
        if (isLongPressing)
            return;

        Log.v(TAG, "[longPress] isLongPressing=" + isLongPressing);

        if (Float.compare(currentSnapAngle, TIMER_10H_ANGLE) == 0) {
            // todo
        } else if (Float.compare(currentSnapAngle, SMART_MODE_ANGLE) == 0) {
            // todo
        } else if (Float.compare(currentSnapAngle, TIMER_15H_ANGLE) == 0) {
            // todo
        }
    }

    long lastTap = System.currentTimeMillis();

    private void onTap(Integer duration, TouchViewGroup touchViewGroup) {
        long currentTime = System.currentTimeMillis();
        Log.v(TAG, "[onTap] duration=" + duration + " lastTap=" + lastTap + " currentTime=" + currentTime + " difference=" + (currentTime - lastTap) + " tapTimeOut=" + ViewConfiguration.getDoubleTapTimeout());
        if (currentTime - lastTap < ViewConfiguration.getDoubleTapTimeout()) {
            return;
        }
        lastTap = currentTime;

        Log.v(TAG, "[onTap]");
    }

    @Override
    protected void onPause() {
        super.onPause();

        cancelAnimations();
    }

    private void cancelAnimations() {
        Log.v(TAG, "[cancelAnimations] " + animators.size());
        for (Animator animator : animators)
            if (animator != null)
                animator.cancel();

        animators.clear();
    }
}