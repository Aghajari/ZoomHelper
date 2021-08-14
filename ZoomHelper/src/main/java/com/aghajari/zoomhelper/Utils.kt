package com.aghajari.zoomhelper

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup


internal object Utils {

    fun findZoomableView(ev: MotionEvent, vararg parents: View): View? {
        if (parents.isEmpty()) return null;
        val p1 = MotionEvent.PointerCoords()
        ev.getPointerCoords(0, p1)

        val p2 = MotionEvent.PointerCoords()
        ev.getPointerCoords(1, p2)

        for (parent in parents) {
            if (!ZoomHelper.isSkippingLayout(parent)) {

                if (isViewInArea(parent, p1.x, p1.y, p2.x, p2.y)
                    && ZoomHelper.isZoomableView(parent)
                ) return parent

                if (parent is ViewGroup) {
                    val v = checkChild(parent, p1.x, p1.y, p2.x, p2.y)
                    if (v != null) return v
                }
            }
        }

        return null
    }

    private fun checkChild(vg: ViewGroup?, x1: Float, y1: Float, x2: Float, y2: Float): View? {
        if (vg == null) return null

        val childCount = vg.childCount
        for (i in 0 until childCount) {
            val child = vg.getChildAt(i)
            if (!ZoomHelper.isSkippingLayout(child)) {
                if (isViewInArea(child, x1, y1, x2, y2)) {
                    if (ZoomHelper.isZoomableView(child)) {
                        return child;
                    } else if (child is ViewGroup) {
                        val v = checkChild(child, x1, y1, x2, y2)
                        if (v != null) return v
                    }
                }
            }
        }
        return null
    }

    private fun isViewInArea(view: View?, x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        if (view == null) return false
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val visibleRect = Rect()
        visibleRect.left = location[0]
        visibleRect.top = location[1]
        visibleRect.right = visibleRect.left + view.width
        visibleRect.bottom = visibleRect.top + view.height

        return (visibleRect.contains(x1.toInt(), y1.toInt())
                && visibleRect.contains(x2.toInt(), y2.toInt()))
    }

    fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Int {
        return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0)).toInt()
    }
}