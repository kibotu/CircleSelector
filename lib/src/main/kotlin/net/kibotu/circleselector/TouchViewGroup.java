package net.kibotu.circleselector;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.common.android.utils.extensions.MathExtensions;
import com.common.android.utils.logging.Logger;
import com.common.android.utils.misc.Vector2;

import net.kibotu.android.deviceinfo.library.display.Dimension;
import net.kibotu.android.deviceinfo.library.display.Display;


/**
 * http://neevek.net/posts/2013/10/13/implementing-onInterceptTouchEvent-and-onTouchEvent-for-ViewGroup.html
 * <p>
 * Purpose of this view is to being able to communicate
 * <pre>
 *      0) consume all events of children and parents
 *      1) click
 *      2) long press
 *      3) dragging
 *      4) angle between pivot and latest touch event point
 * </pre>
 */
public class TouchViewGroup extends RelativeLayout {

    public boolean SHOW_DEBUG_LOGS = false;

    public interface UpdateListener<T> {
        void onUpdate(T t, TouchViewGroup touchViewGroup);
    }

    /**
     * <img src="http://i.imgur.com/Cwtr7ku.png" />
     * <p>
     * We need a 90 degree offset towards {@link Math#atan2}.
     */
    public static final int DEGREE_OFFSET = 90;

    Vector2 previous = new Vector2();
    Vector2 start = new Vector2();

    /**
     * Defines the time in millis when the long press event gets fired.
     */
    private int longPressTimeout;

    /**
     * Distance after which it counts as move gesture.
     */
    private int minDistanceMovingGesture;

    /**
     * Defines if view is currently in a long press gesture.
     */
    private boolean isLongPressing;

    /**
     * Defines if view is currently in active a moving gesture.
     */
    private boolean isBeingDragged;

    /**
     * Holds current angle between pivot and current touch point.
     */
    private double angle;

    /**
     * Used for clamped Y value.
     */
    public Vector2 currentClamped = new Vector2();

    /**
     * Holds current touch coordinate.
     */
    public Vector2 current = new Vector2();

    /**
     * Defines center of the screen.
     */
    public Vector2 screenCenter;

    /**
     * Defines pivot of view.
     */
    public Vector2 center;

    /**
     * Defines pivot offset.
     */
    public Vector2 offset;

    /**
     * Callback for angle update changes.
     */
    @Nullable
    UpdateListener<Double> angleUpdateListener;

    /**
     * Callback for moving gesture changes.
     */
    @Nullable
    UpdateListener<Boolean> onDragUpdateListener;

    /**
     * Callback for long press changes. {@link #longPressTimeout}
     */
    @Nullable
    UpdateListener<Boolean> onLongPressedListener;

    /**
     * Callback for click event.
     */
    @Nullable
    UpdateListener<Integer> onTapListener;

    /**
     * used as buffer to retrieve view location on screen
     */
    int[] location = new int[2];

    // region construct

    public TouchViewGroup(Context context) {
        super(context);
        init();
    }

    public TouchViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    // endregion

    void init() {
        if (isInEditMode())
            return;

        offset = new Vector2();

        final Dimension screenDimensions = Display.getScreenDimensions();
        screenCenter = new Vector2(screenDimensions.width / 2f, screenDimensions.height / 2f);

        longPressTimeout = ViewConfiguration.getLongPressTimeout();
        minDistanceMovingGesture = MathExtensions.dpToPx(20);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <pre>
     * In a nutshell: it can consume touch events before its children.
     *
     *      0) consume all events of children and parents
     * </pre>
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (isInEditMode())
            return false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (SHOW_DEBUG_LOGS)
                    Logger.v(tag(), "[onInterceptTouchEvent] ACTION_DOWN " + event);
                previous.set(event.getRawX(), event.getRawY());
                start.set(event.getRawX(), event.getRawY());
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (SHOW_DEBUG_LOGS)
                    Logger.v(tag(), "[onInterceptTouchEvent] ACTION_UP || ACTION_CANCEL downTime=" + (event.getEventTime() - event.getDownTime()) + " " + event);
                seDragging(false);
                break;

            case MotionEvent.ACTION_MOVE:
                seDragging(true);
                break;

            default:
                if (SHOW_DEBUG_LOGS) Logger.v(tag(), "[onInterceptTouchEvent] Unhandled " + event);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * We communicate the following events to callbacks:
     * <pre>
     *      0) consume all events of children and parents
     *      1) click
     *      2) long press
     *      3) dragging
     *      4) angle between pivot and latest touch event point
     * </pre>
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isInEditMode())
            return false;

        // touch event duration
        final long downTime = event.getEventTime() - event.getDownTime();

        switch (event.getAction()) {

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (SHOW_DEBUG_LOGS)
                    Logger.v(tag(), "[onTouchEvent] ACTION_UP || ACTION_CANCEL downTime=" + downTime + " " + event);
                seDragging(false);

                // downtime below long press duration then fire click, otherwise send cancel long press event
                if (downTime < longPressTimeout) {
                    if (!isMoveGesture())
                        onClick((int) downTime);
                } else
                    onLongPress(false);

                break;

            case MotionEvent.ACTION_MOVE:

                if (SHOW_DEBUG_LOGS)
                    Logger.v(tag(), "[onTouchEvent] ACTION_MOVE downTime=" + downTime + " " + event);

                getLocationInWindow(location);
                current.set(event.getRawX(), event.getRawY());
                currentClamped.set(event.getRawX(), location[1]);
                center = new Vector2(location[0] + getWidth() / 2f + offset.x, location[1] + getHeight() / 2f + offset.y);
                setAngle(new Vector2(currentClamped).sub(center).nor().angle() + DEGREE_OFFSET);

                if (downTime >= longPressTimeout) {
                    if (SHOW_DEBUG_LOGS) Logger.v(tag(), "[ACTION_MOVE] downTime=" + downTime);
                    onLongPress(true);
                }

                boolean consume = !isMoveGesture() || isLongPressing;
                if (SHOW_DEBUG_LOGS)
                    Logger.v(tag(), "[isMoveGesture] start=" + start + " current=" + current + " distance=" + start.dst(current) + " minDistanceMovingGesture=" + minDistanceMovingGesture + " isMoveGesture=" + isMoveGesture() + " isLongPressing=" + isLongPressing + " consume=" + consume);

                // consume touch event inside scroll container like view pagers
                getParent().requestDisallowInterceptTouchEvent(consume);

                seDragging(true);
                break;

            default:
                if (SHOW_DEBUG_LOGS) Logger.v(tag(), "[onTouchEvent] Unhandled " + event);
        }

        return true;
    }

    private boolean isMoveGesture() {
        return isMoveGesture(start, current, minDistanceMovingGesture);
    }

    private static boolean isMoveGesture(Vector2 start, Vector2 end, float distance) {
        return start != null && (end != null && start.dst(end) >= distance);
    }

    // region click

    private void onClick(int duration) {
        Logger.v(tag(), "[onClick]");
        if (onTapListener != null)
            onTapListener.onUpdate(duration, this);
    }

    public void setOnTapListener(@Nullable UpdateListener<Integer> onTapListener) {
        this.onTapListener = onTapListener;
    }

    // endregion

    // region long press

    public boolean isLongPressing() {
        return isLongPressing;
    }

    private void onLongPress(boolean isLongPressing) {
        if (this.isLongPressing != isLongPressing && onLongPressedListener != null)
            onLongPressedListener.onUpdate(isLongPressing, this);

        this.isLongPressing = isLongPressing;
    }

    public void setOnLongPressedListener(@Nullable UpdateListener<Boolean> onLongPressedListener) {
        this.onLongPressedListener = onLongPressedListener;
    }

    public void setLongPressTimeout(int longPressTimeout) {
        this.longPressTimeout = Math.abs(longPressTimeout);
    }

    // endregion

    // region angle

    /**
     * <img src="http://i.imgur.com/Cwtr7ku.png" />
     * <p>
     * Returns angle calculated by the dragging movement based from the screenCenter of the screen.
     *
     * @return Angle in degree.
     */
    public TouchViewGroup setAngleUpdateListener(@Nullable UpdateListener<Double> angleUpdateListener) {
        this.angleUpdateListener = angleUpdateListener;
        return this;
    }

    protected void setAngle(double angle) {
        onAngleUpdate(angle);
        this.angle = angle;
    }

    private void onAngleUpdate(double angle) {
        if (Double.compare(this.angle, angle) != 0 && angleUpdateListener != null)
            angleUpdateListener.onUpdate(angle, this);
    }

    /**
     * Retrieves current angle between pivot of view and latest touch gesture point.
     *
     * @return Euler angle with offset {@link #DEGREE_OFFSET}
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Changes Pivot for computing angle.
     */
    public void setPivotOffset(int offsetX, int offsetY) {
        offset.set(offsetX, offsetY);
        postInvalidate();
    }

    // endregion

    // region dragging

    /**
     * Fires moving gesture event.
     *
     * @param isBeingDragged <code>true</code> if being dragged.
     */
    private void seDragging(boolean isBeingDragged) {
        onDragUpdate(isBeingDragged);
        this.isBeingDragged = isBeingDragged;
    }

    private void onDragUpdate(boolean isBeingDragged) {
        if (this.isBeingDragged != isBeingDragged && onDragUpdateListener != null)
            onDragUpdateListener.onUpdate(isLongPressing, this);
    }

    public void setOnDragUpdateListener(@Nullable UpdateListener<Boolean> onDragUpdateListener) {
        this.onDragUpdateListener = onDragUpdateListener;
    }

    // endregion

    @NonNull
    public String tag() {
        return getClass().getSimpleName();
    }
}
