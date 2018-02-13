package net.kibotu.circleselector

import android.app.Activity
import java.lang.ref.WeakReference

object CircleSelector {

    private var _activity: WeakReference<Activity?>? = null

    var activity: Activity?
        set(value) {
            _activity = WeakReference(value)
        }
        get() = _activity?.get()
}
