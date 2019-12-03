package jp.kai.forcelayout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Align.CENTER
import android.graphics.Paint.Style.FILL_AND_STROKE
import android.graphics.PointF
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.Edge
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.GraphStyle
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.LinkProperty
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.Node
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.NodeProperty
import jp.kai.forcelayout.properties.ForceProperty
import java.lang.Math.round
import java.lang.Math.sqrt

/**
 * Created by kai on 2017/05/01.
 * Main Class
 */

open class Forcelayout : View {
  /** instance */
  private val forceProperty: ForceProperty
  private val nodeProperty: NodeProperty = NodeProperty()
  private val linkProperty: LinkProperty = LinkProperty()

  private var targetNode = -1

  private var touchX: Float = 0f
  private var touchY: Float = 0f
  private val viewConfiguration by lazy {
    ViewConfiguration.get(this.context)
  }
  private var pointF: PointF = PointF()
  private var scaleFactor = 1f
  private var detector: ScaleGestureDetector

  private var translateX = 0f
  private var translateY = 0f
  private var previousTranslateX = 0f
  private var previousTranslateY = 0f
  private var downX = 0f
  private var downY: Float = 0f

  var nodeMinLeft: Node? = null
  var nodeMaxRight: Node? = null
  var nodeMinTop: Node? = null
  var nodeMaxBottom: Node? = null

  // TODO enumか何かで管理する: pan/expand/drag
  private var zooming: Boolean = false

  init {
    detector = ScaleGestureDetector(context, ScaleListener())
  }

  constructor(mContext: Context) : super(mContext) {
    forceProperty = ForceProperty(mContext, this)
  }

  constructor(
    mContext: Context,
    attrs: AttributeSet
  ) : super(mContext, attrs) {
    forceProperty = ForceProperty(mContext, this)
  }

  constructor(
    mContext: Context,
    attrs: AttributeSet,
    defStyleAttr: Int
  ) : super(mContext, attrs, defStyleAttr) {
    forceProperty = ForceProperty(mContext, this)
  }

  /**
   * Create Builders
   */
  fun node(): NodeProperty {
    return nodeProperty.prepare()
  }

  fun link(): LinkProperty {
    return linkProperty.prepare()
  }

  fun with(): ForceProperty {
    return forceProperty.prepare()
  }

  /**
   * 圆是否可以拖拽？
   */
  var touchDragEnable = false

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {

    if (!forceProperty.isReady) {
      return super.onTouchEvent(event)
    }


    this.touchX = event.x
    this.touchY = event.y
    detector.onTouchEvent(event)

    val toucheHandle = toucheHandle(event)
    if (toucheHandle) {
      parent.requestDisallowInterceptTouchEvent(true)
    } else {
      parent.requestDisallowInterceptTouchEvent(false)
    }

    //请勿相信IDE直接true，具体原因请读事件分发源码
    return super.onTouchEvent(event) || toucheHandle
  }

  private fun toucheHandle(event: MotionEvent): Boolean {

    var x: Float
    var y: Float
    var pointerIndex: Int
    var activePointerId = 0
    val tempNodes = forceProperty.nodes

    when (event.action) {

      MotionEvent.ACTION_DOWN -> {
        downX = event.getX()
        downY = event.getY()
        activePointerId = event.getPointerId(0)
        //TODO 位置補正はタッチ時の判定でやったほうが楽そうなので、頑張る translateとscaleFactorあたりを調査
        if (targetNode == -1) {
          if (touchDragEnable) {
            for (i in tempNodes.indices) {
              if (tempNodes[i].x + tempNodes[i].width >= (touchX - translateX) / scaleFactor &&
                  tempNodes[i].x <= (touchX - translateX) / scaleFactor &&
                  tempNodes[i].y + tempNodes[i].height >= (touchY - translateY) / scaleFactor &&
                  tempNodes[i].y <= (touchY - translateY) / scaleFactor
              ) {

                targetNode = i
              }
            }
          }

        } else {
          tempNodes[targetNode].x =
            ((touchX - translateX) / scaleFactor - tempNodes[targetNode].width / 2)
          tempNodes[targetNode].y =
            ((touchY - translateY) / scaleFactor - tempNodes[targetNode].height / 2)

        }

      }

      MotionEvent.ACTION_MOVE -> {

//        pointerIndex = event.findPointerIndex(activePointerId)
        x = event.getX()
        y = event.getY()

//        if ((x - downX).absoluteValue < viewConfiguration.scaledTouchSlop && (y - downY).absoluteValue < viewConfiguration.scaledTouchSlop) {

//        } else {
        if (!detector.isInProgress) {

          if (targetNode == -1) {

//            val throttleTouch = viewConfiguration.scaledTouchSlop * 2

            val yOffset = y - downY
            val xOffset = x - downX

            val nowYOfset = (translateY + yOffset)
            val nowXOfset = (translateX + xOffset)

            val bottomNode = nodeMaxBottom
            /**
             *  边界检测
             */
            if (translateBorderBottom(bottomNode, nowYOfset)) {
              parent.requestDisallowInterceptTouchEvent(false)
              return false
            }

            /**
             *  边界检测
             */
            if (translateBorderTop(nodeMinTop, nowYOfset)) {
              parent.requestDisallowInterceptTouchEvent(false)
              return false
            }
            /**
             *  边界检测
             */
            if (translateBorderLeft(nodeMinLeft, nowXOfset)) {
              parent.requestDisallowInterceptTouchEvent(false)
              return false
            }
            /**
             *  边界检测
             */
            if (translateBorderRight(nodeMaxRight, nowXOfset)) {
              parent.requestDisallowInterceptTouchEvent(false)
              return false
            }



            translateX += xOffset
            translateY += yOffset
            postInvalidate()
          } else {
            tempNodes[targetNode].x =
              ((touchX - translateX) / scaleFactor - tempNodes[targetNode].width / 2)
            tempNodes[targetNode].y =
              ((touchY - translateY) / scaleFactor - tempNodes[targetNode].height / 2)
          }
        }
        downX = x
        downY = y

      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        activePointerId = -1
        zooming = false
        targetNode = -1

      }

      MotionEvent.ACTION_POINTER_UP -> {
        zooming = false
        pointerIndex = (event.action and MotionEvent.ACTION_POINTER_INDEX_MASK
            shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
        val pointerId = event.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
          val newPointerIndex = if (pointerIndex == 0) 1 else 0
          downX = event.getX(newPointerIndex)
          downY = event.getY(newPointerIndex)
          activePointerId = event.getPointerId(newPointerIndex)
        }
      }

      MotionEvent.ACTION_POINTER_DOWN -> {
        midPoint(pointF, event)
        zooming = true
        targetNode = -1
      }

    }

    return true
  }

  private fun translateBorderRight(
    nodeMinRight: Node?,
    nowXOffset: Float
  ): Boolean {

    if (nodeMinRight != null
        && nowXOffset < translateX
        && nowXOffset < 0
        && measuredWidth / 2 > nowXOffset + (nodeMinRight.x + nodeMinRight.width / 2) * scaleFactor

    ) {
      return true
    }
    return false
  }

  private fun translateBorderBottom(
    bottomNode: Node?,
    nowYOffset: Float
  ): Boolean {
    if (bottomNode != null
        && nowYOffset < translateY
        && nowYOffset < 0
        && measuredHeight / 2 > (bottomNode.y + bottomNode.height / 2) * scaleFactor - Math.abs(
            nowYOffset
        )

    ) {
      return true
    }
    return false
  }

  private fun translateBorderLeft(
    leftNode: Node?,
    nowYOffset: Float
  ): Boolean {
    if (leftNode != null
        && nowYOffset > translateY
        && nowYOffset > 0
        && (measuredWidth / 2) < nowYOffset + (leftNode.x - leftNode.height / 2) * scaleFactor

    ) {
      return true
    }
    return false
  }

  private fun translateBorderTop(
    topNode: Node?,
    nowYOffset: Float
  ): Boolean {

    if (topNode == null) {
      return false
    }



    if (nowYOffset > 0
        && nowYOffset > translateY
        && measuredHeight / 2 < nowYOffset + (topNode.y - topNode.height / 2) * scaleFactor

    ) {
      return true
    }
    return false
  }

  private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector): Boolean {

      val pScale: Float = scaleFactor

      scaleFactor *= detector.scaleFactor

      scaleFactor *= detector.scaleFactor
      scaleFactor = MIN_ZOOM.coerceAtLeast(scaleFactor.coerceAtMost(MAX_ZOOM))

//      if ((newScale - scaleFactor).absoluteValue < 0.05) {
//
//        return true
//      } else {
//      }

      if (!(pScale.toDouble() == 0.1 && scaleFactor.toDouble() == 0.1 || pScale == 5f && scaleFactor == 5f)) {
        val focusX = detector.focusX
        val focusY = detector.focusY
//        translateX += (focusX - translateX) * (1 - detector.scaleFactor)
//        translateY += (focusY - translateY) * (1 - detector.scaleFactor)
        var newTranslateY = (focusY - translateY) * (1 - detector.scaleFactor) + translateY
        var newTranslateX = (focusX - translateX) * (1 - detector.scaleFactor) + translateX

        if (translateBorderBottom(nodeMaxBottom, newTranslateY)) {
//          translateY=newTranslateY
        } else {

          if (translateBorderTop(nodeMinTop, newTranslateY)) {

          } else {

            if (translateBorderLeft(nodeMinLeft, newTranslateX)) {
            } else {
              if (translateBorderRight(nodeMaxRight, newTranslateX)) {
              } else {
                translateX = newTranslateX
                translateY = newTranslateY;

              }
            }
          }

        }
//
      }

      postInvalidate()
      return true
    }
  }

  public override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    forceProperty.displayHeight = measuredHeight.toFloat()
    forceProperty.displayWidth = measuredWidth.toFloat()
  }

  open fun drawLoadingTip(canvas: Canvas) {
    val paint = Paint()
    paint.isAntiAlias = true
    paint.style = FILL_AND_STROKE
//    paint.color = ContextCompat.getColor(this@ForceView.getContext(), R.color.color_text_main)
    paint.color = Color.RED
    paint.textAlign = CENTER
    val fontMetrics = paint.fontMetrics
    val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    val baseline = measuredHeight / 2 + distance
    paint.textSize = 40
        .toFloat()
    canvas.drawText("加载中...", measuredWidth / 2.toFloat(), baseline, paint)
  }

  // draw function
  override fun dispatchDraw(canvas: Canvas) {

    val tempNodes = forceProperty.nodes
    val tempEdges = forceProperty.edges

    val paint = Paint()

    // scale the canvas
    canvas.scale(scaleFactor, scaleFactor, pointF.x, pointF.y)

    canvas.translate(translateX / scaleFactor, translateY / scaleFactor)

    if (targetNode != -1) {
      tempNodes[targetNode].x =
        ((touchX - translateX) / scaleFactor - tempNodes[targetNode].width / 2)// / scaleFactor
      tempNodes[targetNode].y =
        ((touchY - translateY) / scaleFactor - tempNodes[targetNode].height / 2)// / scaleFactor
    }

    //绘制连接线
    if (forceProperty.isReady) {
      //draw link's line
      for (i in tempEdges.indices) {
        canvas.save()
        val linePaint = Paint(paint)
        drawJoinLine(i, linePaint, canvas, tempEdges, tempNodes)
        canvas.restore()
      }

      paint.color = Color.BLACK


      for (i in tempNodes.indices) {
        //画球
        canvas.save()
        val nodePaint = Paint(paint)
        drawNode(tempNodes[i], canvas, nodePaint)
        canvas.restore()

        //画对应的文字
        canvas.save()
        val textPaint = Paint(paint)
        drawText(tempNodes[i], canvas, textPaint)
        canvas.restore()
      }

      val bottomNode = nodeMaxBottom ?: return

      val y = bottomNode.y + bottomNode.height / 2

    } else {
      drawLoadingTip(canvas)
    }

  }

  private fun drawText(
    node: Node,
    canvas: Canvas,
    paint: Paint
  ) {
    if (node == null) {
      return
    }

    val cx = (node.x).toFloat()
    val cy = (node.y).toFloat()
    paint.color = node.textColor
    paint.textSize = node.textSize.toFloat()
    paint.textAlign = CENTER

    var fontMetrics = paint.fontMetrics

    var radius = node.width / 2

    var text = node.nodename

    if (text == null) {
      text = ""
    }
    val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    val baseline = cy + distance

    var textHeight = fontMetrics.bottom - fontMetrics.top

    val w: Double =
      Math.sqrt(4 * radius * radius - textHeight * textHeight.toDouble())

    val textWidth: Float = paint.measureText(text)

    var n: Float
    if (w >= textWidth || node.level >= 2) { // 一行能装下

      if (node.level >= 2) { //这个level的节点文字被绘制在圆圈外
        canvas.drawText(text, cx, (cy - radius - distance).toFloat(), paint)
      } else {
        canvas.drawText(text, cx, baseline, paint)
      }

    } else { // 一行装不下

      val th: Float = textHeight * 2
      // 如果能够排成两行，并且排成两行之后，每行减去 padding 之后还能装下内容
      var ww: Double = sqrt(
          4 * radius * radius - th * th.toDouble()
      )

      if (th > radius) {
        val end = round(ww.toFloat() / textWidth * text.length)
        if (end < text.length) {
          canvas.drawText(
              text.substring(0, end), cx, cy - textHeight * 0.5f + distance,
              paint
          )
          val t: String
          // 两行都装不下，就省略
          t = if (textWidth > 2 * ww) {
            text.substring(end, 2 * end - 1) + ".."
          } else {
            text.substring(end)
          }
          canvas.drawText(
              t, cx, cy - textHeight * 0.5f + distance + textHeight * 0.5f + distance, paint
          )
        } else {
          canvas.drawText(text, cx, baseline, paint)
        }
      } else {
        val end = round(w.toFloat() / textWidth * text.length)
        val t = text.substring(0, end - 1) + ".."
        canvas.drawText(t, cx, baseline, paint)
      }
    }
  }

  /**
   * 绘制两个球之间的连接线
   */
  private fun drawJoinLine(
    position: Int,
    paint: Paint,
    canvas: Canvas,
    tempEdges: List<Edge>,
    tempNodes: List<Node>
  ) {

    val edge = tempEdges[position]


    if (edge.group) {

      val fromNode = tempNodes[edge.from]
      val toNode = tempNodes[edge.to]

      val x1 = (fromNode.x).toFloat()
      val y1 =
        (fromNode.y).toFloat()

      val x2 = (toNode.x).toFloat()
      val y2 = (toNode.y).toFloat()

      paint.strokeWidth = GraphStyle.linkWidth
      paint.color = GraphStyle.linkColor

      canvas.drawLine(x1, y1, x2, y2, paint)

    }
  }

  /**
   * 绘制圆圈
   */
  private fun drawNode(
    node: Node,
    canvas: Canvas,
    paint: Paint
  ) {

    val cx = (node.x).toFloat()
    val cy = (node.y).toFloat()
    val radius = (node.height / 2).toFloat()


    paint.shader = LinearGradient(
        0f, cy - radius, 0f, cy + radius, node.getGradientColor(), floatArrayOf(0.5f, 1f),
        TileMode.MIRROR
    )

    canvas.drawCircle(
        cx,
        cy,
        radius, paint
    )

    paint.textSize = GraphStyle.fontSize

    paint.color = GraphStyle.fontColor
    paint.color = Color.BLACK

    val fontMetrics = paint.fontMetrics
    val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    paint.textAlign = CENTER
    paint.shader = null

  }

  private fun midPoint(
    pointF: PointF,
    motionEvent: MotionEvent
  ) {
    val x = motionEvent.getX(0) + motionEvent.getX(1)
    val y = motionEvent.getY(0) + motionEvent.getY(1)
    pointF.set(x / 2, y / 2)
  }
}