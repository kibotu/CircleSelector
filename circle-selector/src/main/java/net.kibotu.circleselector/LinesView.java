package net.kibotu.circleselector;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.kibotu.circleselector.TouchViewGroup.dpToPx;

/**
 * Created by <a href="https://about.me/janrabe">Jan Rabe</a>.
 */

public class LinesView extends View {

    Paint paint;
    List<Line> lines;

    public int strokeColor;
    public int strokeStrength;

    public LinesView(Context context) {
        super(context);
        init();
    }

    public LinesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LinesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        if (isInEditMode())
            return;

        paint = new Paint();
        lines = new ArrayList<>();
        strokeColor = ContextCompat.getColor(getContext(), android.R.color.black);
        strokeStrength = dpToPx(1);

        paint.setColor(strokeColor);
        paint.setStrokeWidth(5);
    }

    public void addLine(Line line) {
        lines.add(line);

        postInvalidate();
    }

    public void addLine(Vector2 start, Vector2 end) {
        lines.add(new Line(start, end));

        postInvalidate();
    }

    public void setLines(Line... l) {
        if (isEmpty(l))
            return;
        lines.clear();
        lines.addAll(Arrays.asList(l));

        postInvalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isInEditMode())
            return;
        for (Line line : lines)
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, paint);
    }

    public static <T> boolean isEmpty(T[] l) {
        return l == null || l.length < 1;
    }
}
