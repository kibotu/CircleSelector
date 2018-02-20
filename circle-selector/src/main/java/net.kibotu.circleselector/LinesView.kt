package net.kibotu.circleselector

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * Created by [Jan Rabe](https://about.me/janrabe).
 */

class LinesView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint: Paint = Paint()
    private var lines: MutableList<Line> = ArrayList()

    var strokeColor: Int = 0
    var strokeStrength: Int = 0

    init {

        strokeColor = ContextCompat.getColor(context, android.R.color.black)

        if (!isInEditMode) {
            strokeStrength = CircleSelector.dpToPx(1)
        }

        paint.color = strokeColor
        paint.strokeWidth = 5f
    }

    fun addLine(line: Line) {
        lines.add(line)

        postInvalidate()
    }

    fun addLine(start: Vector2, end: Vector2) {
        lines.add(Line(start, end))

        postInvalidate()
    }

    fun setLines(vararg l: Line) {
        if (isEmpty(l))
            return
        lines.clear()
        lines.addAll(Arrays.asList(*l))

        postInvalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        if (isInEditMode)
            return
        for (line in lines)
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, paint)
    }

    companion object {

        fun <T> isEmpty(l: Array<T>?): Boolean {
            return l == null || l.isEmpty()
        }
    }
}
