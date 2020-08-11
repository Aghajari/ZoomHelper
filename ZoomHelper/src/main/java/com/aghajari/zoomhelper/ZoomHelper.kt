package com.aghajari.zoomhelper

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author AmirHossein Aghajari
 * @version 1.0.1
 */
class ZoomHelper {

    /**
     * get current zooming view
     */
    var zoomableView: View? = null
        private set

    /**
     * get current zooming view parent in activity
     */
    var zoomableViewParent: ViewGroup? = null
        private set

    /**
     * get zoomLayout's dialog
     */
    var dialog: Dialog? = null
        private set

    private var shadowView: View? = null

    /**
     * @return true if currently is zooming by user
     */
    var isZooming: Boolean = false
        private set

    private var isAnimating: Boolean = false

    private var onZoomStateChangedListener = arrayListOf<OnZoomStateChangedListener>()
    private var onZoomScaleChangedListener = arrayListOf<OnZoomScaleChangedListener>()
    private var onZoomLayoutCreatorListener = arrayListOf<OnZoomLayoutCreatorListener>()

    private var originalXY: IntArray? = null
    private var twoPointerCenter: IntArray? = null
    private var viewIndex:Int? = 0
    private var originalDistance:Int = 0
    private var pivotX:Float = 0f
    private var pivotY:Float = 0f
    private var viewLayoutParams:ViewGroup.LayoutParams? =null
    private var viewFrameLayoutParams:FrameLayout.LayoutParams? =null
    private var placeHolderView:PlaceHolderView? = null

    /**
     * max zoom scale, or -1 for unlimited [Float.MAX_VALUE]
     * default: -1 [Float.MAX_VALUE]
     */
    var maxScale = -1f
    /**
     * min zoom scale, or -1 for unlimited [Float.MIN_VALUE]
     * default: 1f
     */
    var minScale = 1f

    var shadowAlphaFactory = 6f

    /**
     * max shadow alpha
     * default: 0.8f
     */
    var maxShadowAlpha = 0.8f

    /**
     * shadow color
     * default: black
     */
    var shadowColor = Color.BLACK

    /**
     * zoom layout theme
     * default: Translucent_NoTitleBar_Fullscreen
     */
    var layoutTheme = R.style.ZoomLayoutStyle
    //var layoutTheme = android.R.style.Theme_Translucent_NoTitleBar_Fullscreen

    /**
     * dismiss animation duration
     * default: android default shortAnimTime
     */
    var dismissDuration:Long = -1

    var placeHolderDelay:Long = 80
    var placeHolderDismissDelay:Long = 30
    private var placeHolderEnabled = true
    var isPlaceHolderEnabled : Boolean
        get() = placeHolderEnabled
        set(value) {
            placeHolderEnabled = value
            placeHolderView?.isEnabled = value
        }

    private var lastScale = -10f
    private var enabled = true

    var isEnabled:Boolean
        get() = enabled
        set(value) {
            enabled = value
            if (zoomableView!=null && !isAnimating){
                animateDismiss()
            }
        }

    private fun init(context: Context){
        if (dismissDuration<0)
        dismissDuration = context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }

    /**
     * handle touch event for an specific View or ViewGroup and its children
     * call this method in [Activity.dispatchTouchEvent]
     */
    fun dispatchTouchEvent(ev: MotionEvent,parent:View) :Boolean{
        return load(ev,parent)
    }

    /**
     * handle touch event for an Activity
     * call this method in [Activity.dispatchTouchEvent]
     */
    fun dispatchTouchEvent(ev: MotionEvent,parent:Activity) :Boolean{
        return load(ev,parent.findViewById(android.R.id.content) as View)
    }

    /**
     * handle touch event for specific Views or ViewGroups and their children
     * call this method in [Activity.dispatchTouchEvent]
     */
    fun dispatchTouchEvent(ev: MotionEvent, vararg parents: View):Boolean{
        return load(ev,*parents)
    }

    private fun load(ev: MotionEvent, vararg parents: View):Boolean{
        if (!isEnabled) return isAnimating
        if (parents.isEmpty()) return false
        val context = parents[0].context
        init(context)

        if (ev.pointerCount>=2){
            if (zoomableView==null){
                var view:View? = Utils.findZoomableView(ev, *parents) ?: return false;
                zoomableView = view;

                reset()
                isAnimating = false
                isZooming = true
                originalXY = IntArray(2)
                view?.getLocationOnScreen(originalXY)

                var frameLayout = FrameLayout(context)
                shadowView = View(context)
                shadowView?.setBackgroundColor(shadowColor)
                shadowView?.alpha = 0f

                frameLayout.addView(shadowView,FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT))

                dialog = Dialog(context,layoutTheme)
                dialog?.addContentView(frameLayout,ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT))
                dialogCreated(frameLayout)
                dialog?.show()

                zoomableViewParent = view?.parent as ViewGroup;
                viewIndex = zoomableViewParent?.indexOfChild(zoomableView)
                viewLayoutParams = view.layoutParams

                viewFrameLayoutParams = FrameLayout.LayoutParams(view.width,view.height)
                viewFrameLayoutParams?.leftMargin = originalXY!![0]
                viewFrameLayoutParams?.topMargin = originalXY!![1]

                placeHolderView = PlaceHolderView(view)
                placeHolderView?.isEnabled = placeHolderEnabled

                var index: Int = viewIndex ?:0
                zoomableViewParent?.addView(placeHolderView,index,viewLayoutParams)
                zoomableViewParent?.removeView(view)
                frameLayout.addView(zoomableView,viewFrameLayoutParams)

                if (placeHolderEnabled) {
                    view.postDelayed(Runnable {
                        placeHolderView!!.visibility = View.INVISIBLE
                    }, placeHolderDelay)
                }

                val p1 = MotionEvent.PointerCoords()
                ev.getPointerCoords(0,p1)

                val p2 = MotionEvent.PointerCoords()
                ev.getPointerCoords(1,p2)

                originalDistance = Utils.getDistance(p1.x.toDouble(),p1.y.toDouble(),p2.x.toDouble(),p2.y.toDouble())
                twoPointerCenter = intArrayOf(((p2.x+p1.x)/2).toInt(),((p2.y+p1.y)/2).toInt())

                pivotX = (ev.rawX - originalXY!![0])
                pivotY = (ev.rawY - originalXY!![1])

                stateChanged()
            }else{
                val p1 = MotionEvent.PointerCoords()
                ev.getPointerCoords(0,p1)

                val p2 = MotionEvent.PointerCoords()
                ev.getPointerCoords(1,p2)

                var newCenter = intArrayOf(((p2.x+p1.x)/2).toInt(),((p2.y+p1.y)/2).toInt())
                var currentDistance = Utils.getDistance(p1.x.toDouble(),p1.y.toDouble(),p2.x.toDouble(),p2.y.toDouble())
                var pctIncrease = (currentDistance.toDouble() - originalDistance.toDouble()) / originalDistance.toDouble();

                zoomableView!!.pivotX = pivotX
                zoomableView!!.pivotY = pivotY

                var scale = (1+pctIncrease).toFloat()
                if (minScale!=-1f) scale = max(minScale,scale)
                if (maxScale!=-1f) scale = min(maxScale,scale)
                scale = max(Float.MIN_VALUE,scale)
                scale = min(Float.MAX_VALUE,scale)

                zoomableView!!.scaleX = scale
                zoomableView!!.scaleY = scale

                if (twoPointerCenter!=null && originalXY!=null){
                    updateZoomableViewMargins((newCenter[0]- twoPointerCenter!![0]+ originalXY!![0]).toFloat(),
                        (newCenter[1]- twoPointerCenter!![1]+ originalXY!![1].toFloat()))
                }

                scaleChanged(scale,ev)
                if (lastScale!=-10f && lastScale==scale) return true
                lastScale = scale
                shadowView?.alpha = max(min(maxShadowAlpha,abs(pctIncrease/shadowAlphaFactory).toFloat()),0f)
            }
            return true
        }else{
            if (zoomableView!=null){
                if (!isAnimating){
                    animateDismiss();
                }
                return true
            }
        }

        return false
    }

    private fun reset(){
        lastScale = -10f
    }

    private fun animateDismiss(){
        reset()
        if (zoomableView==null || originalXY==null) return
        isAnimating = true;
        val scaleYStart = zoomableView!!.scaleY;
        val scaleXStart = zoomableView!!.scaleX;
        val leftMarginStart = viewFrameLayoutParams!!.leftMargin
        val topMarginStart = viewFrameLayoutParams!!.topMargin
        val alphaStart = shadowView!!.alpha

        val scaleYEnd = 1f;
        val scaleXEnd = 1f;
        val leftMarginEnd = originalXY!![0]
        val topMarginEnd = originalXY!![1]
        val alphaEnd = 0f

        val valueAnimator = ValueAnimator.ofFloat(0f,1f)
        valueAnimator.duration = dismissDuration
        valueAnimator.addUpdateListener{
            val animatedFraction = it.animatedFraction
            if (zoomableView!=null){
                updateZoomableView(animatedFraction,scaleYStart,scaleXStart,leftMarginStart
                    ,topMarginStart,scaleXEnd,scaleYEnd,leftMarginEnd,topMarginEnd)
            }
            if (shadowView!=null){
                shadowView?.alpha = max(min(maxShadowAlpha,((alphaEnd - alphaStart) * animatedFraction) + alphaStart),0f)
            }
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                end()
            }

            override fun onAnimationCancel(animation: Animator) {
                end()
            }

            private fun end(){
                if (zoomableView!=null){
                    updateZoomableView(1f,scaleYStart,scaleXStart,leftMarginStart
                                    ,topMarginStart,scaleXEnd,scaleYEnd,leftMarginEnd,topMarginEnd)
                }
                dismissDialogAndViews()
                valueAnimator.removeAllListeners()
                valueAnimator.removeAllUpdateListeners()
            }

            override fun onAnimationStart(animation: Animator){}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        valueAnimator.start()

    }

    private fun updateZoomableView(animatedFraction:Float,scaleYStart:Float,scaleXStart:Float,
                                   leftMarginStart:Int,topMarginStart:Int,scaleXEnd:Float,scaleYEnd:Float,
                                   leftMarginEnd:Int,topMarginEnd:Int){
        zoomableView?.scaleX =((scaleXEnd-scaleXStart)*animatedFraction)+scaleXStart
        zoomableView?.scaleY =((scaleYEnd-scaleYStart)*animatedFraction)+scaleYStart
        scaleChanged(zoomableView!!.scaleX,null)

        updateZoomableViewMargins(((leftMarginEnd-leftMarginStart)*animatedFraction)+leftMarginStart,
            ((topMarginEnd-topMarginStart)*animatedFraction)+topMarginStart)
    }

    private fun updateZoomableViewMargins(left:Float,top:Float){
        if (zoomableView!=null && viewFrameLayoutParams!=null){
            viewFrameLayoutParams!!.leftMargin = left.toInt()
            viewFrameLayoutParams!!.topMargin = top.toInt()
            zoomableView?.layoutParams = viewFrameLayoutParams
        }
    }

    private fun dismissDialogAndViews(){
        if (zoomableView!=null){
            zoomableView?.visibility = View.VISIBLE
            if (placeHolderEnabled) {
                placeHolderView?.visibility = View.VISIBLE
                placeHolderView?.postDelayed(Runnable {
                    val parent = zoomableView!!.parent as ViewGroup;
                    parent.removeView(zoomableView)
                    zoomableViewParent?.addView(zoomableView!!, viewIndex!!, viewLayoutParams)
                    zoomableViewParent?.removeView(placeHolderView)
                    dismissDialog()
                }, placeHolderDismissDelay)
            }else{
                val parent = zoomableView!!.parent as ViewGroup;
                parent.removeView(zoomableView)
                zoomableViewParent?.addView(zoomableView!!, viewIndex!!, viewLayoutParams)
                zoomableViewParent?.removeView(placeHolderView)
                dismissDialog()
            }
        }else{
            dismissDialog()
        }

        isAnimating = false
        isZooming = false
        stateChanged()
    }

    private fun dismissDialog(){
        if (dialog!=null){
            dialog?.dismiss()
            dialog = null
        }

        shadowView = null
        placeHolderView = null

        if (zoomableView!=null){
            zoomableView?.invalidate()
            zoomableView = null;
        }

        isAnimating = false
        isZooming = false
    }

    private fun stateChanged () {
        if (zoomableView==null) return
        for (l in onZoomStateChangedListener){
            l.onZoomStateChanged(this,zoomableView!!,isZooming)
        }
    }

    private fun scaleChanged(scale:Float,ev: MotionEvent?){
        if (zoomableView==null) return
        for (l in onZoomScaleChangedListener){
            l.onZoomScaleChanged(this,zoomableView!!,scale,ev)
        }
    }

    private fun dialogCreated(layout:FrameLayout){
        if (zoomableView==null) return
        for (l in onZoomLayoutCreatorListener){
            l.onZoomLayoutCreated(this,zoomableView!!,layout)
        }
    }

    fun addOnZoomStateChangedListener (listener: OnZoomStateChangedListener){
        onZoomStateChangedListener.add(listener)
    }

    fun removeOnZoomStateChangedListener(listener: OnZoomStateChangedListener){
        onZoomStateChangedListener.remove(listener)
    }

    fun addOnZoomScaleChangedListener (listener: OnZoomScaleChangedListener){
        onZoomScaleChangedListener.add(listener)
    }

    fun removeOnZoomScaleChangedListener(listener: OnZoomScaleChangedListener){
        onZoomScaleChangedListener.remove(listener)
    }

    fun addOnZoomLayoutCreatorListener (listener: OnZoomLayoutCreatorListener){
        onZoomLayoutCreatorListener.add(listener)
    }

    fun removeOnZoomLayoutCreatorListener(listener: OnZoomLayoutCreatorListener){
        onZoomLayoutCreatorListener.remove(listener)
    }


    interface OnZoomStateChangedListener {
        fun onZoomStateChanged(zoomHelper: ZoomHelper,zoomableView :View,isZooming: Boolean)
    }

    interface OnZoomScaleChangedListener {
        fun onZoomScaleChanged(zoomHelper: ZoomHelper,zoomableView :View,scale: Float,event: MotionEvent?)
    }

    interface OnZoomLayoutCreatorListener {
        fun onZoomLayoutCreated(zoomHelper: ZoomHelper,zoomableView: View,zoomLayout: FrameLayout);
    }

    /**
     * dismiss zoom layout without animation
     * @see isEnabled
     */
    fun dismiss(){
        dismissDialog()
    }

    /**
     * dismiss and release the static ZoomHelper
     * All the static ZoomHelper settings will change to the default value!
     * Listeners will
     */
    fun release(){
        dismiss()
        InstanceState.release()
    }

    companion object {
        /**
         * @return true if view is zoomable
         */
        fun isZoomableView(view: View) = (view.getTag(R.id.zoomable)!=null)

        /**
         * Set view to be zoomable
         */
        fun addZoomableView(view:View) = view.setTag(R.id.zoomable,Object())

        /**
         * Set view to be zoomable
         * @param tag any tag
         * @see getZoomableViewTag (View)
         */
        fun addZoomableView(view:View,tag:Any?) {
            var vt = tag;
            if (vt==null) vt = Object()
            view.setTag(R.id.zoomable,vt);
        }

        /**
         * @return zoomableView tag or null if view is not zoomable
         * @see addZoomableView(View,Object)
         */
        fun getZoomableViewTag(view:View): Any? = view.getTag(R.id.zoomable)

        /**
         * @see addZoomableView
         */
        fun removeZoomableView(view:View) = view.setTag(R.id.zoomable, null)

        fun getInstance() = InstanceState.getZoomHelper()

        /**
         * @return true if layout is skipping
         * @see skipLayout(View,Boolean)
         */
        fun isSkippingLayout(view:View) = (view.getTag(R.id.skip_zoom_layout)!= null)

        /**
         * skip layout and all layout's zoomable children
         */
        fun skipLayout (view:View,skip:Boolean) = view.setTag(R.id.skip_zoom_layout,if (skip) Object() else null)
    }

}