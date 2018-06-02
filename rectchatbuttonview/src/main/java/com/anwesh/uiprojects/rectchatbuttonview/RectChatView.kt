package com.anwesh.uiprojects.rectchatbuttonview

/**
 * Created by anweshmishra on 02/06/18.
 */

import android.app.Activity
import android.view.View
import android.content.Context
import android.view.MotionEvent
import android.graphics.*

val RECT_CHAT_NODES : Int = 5

class RectChatView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RectChatNode(var i : Int , val state : State = State()) {

        private var next : RectChatNode? = null

        private var prev : RectChatNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < RECT_CHAT_NODES - 1) {
                next = RectChatNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            prev?.draw(canvas, paint)
            paint.color = Color.parseColor("#e67e22")
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val h_Gap : Float = (h) / (2 * RECT_CHAT_NODES + 1)
            val y : Float = 1.5f * h_Gap + (i * 2 * h_Gap)
            canvas.save()
            canvas.translate(w/2, y)
            canvas.scale(1f - 2 * (i%2), 1f)
            canvas.save()
            canvas.translate(-w/2, 0f)
            canvas.drawCircle(h_Gap, 0f, (h_Gap / 3) * state.scales[0], paint)
            canvas.save()
            val clipPath : Path = Path()
            clipPath.addRect(RectF(h_Gap * 2, -h_Gap/2, w * state.scales[1], h_Gap/2), Path.Direction.CCW)
            canvas.clipPath(clipPath)
            val path : Path = Path()
            path.moveTo(h_Gap * 2, 0f)
            path.lineTo(h_Gap * 3, -h_Gap/2)
            path.lineTo(h_Gap * 3, h_Gap/2)
            canvas.drawPath(path, paint)
            canvas.drawRect(RectF(3 * h_Gap, -h_Gap/2, w - h_Gap, h_Gap/2), paint)
            canvas.restore()
            canvas.restore()
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RectChatNode {
            var curr : RectChatNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RectChat (var i : Int) {

        var curr : RectChatNode = RectChatNode(0)

        var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : RectChatView) {

        private val animator : Animator = Animator(view)

        private val rectChat : RectChat = RectChat(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            rectChat.draw(canvas, paint)
            animator.animate {
                rectChat.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rectChat.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) {
            val view : RectChatView = RectChatView(activity)
            activity.setContentView(view)
        }
    }
}