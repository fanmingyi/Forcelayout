package jp.kai.forcelayout.properties

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.Edge
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.Node
import jp.kai.forcelayout.ATTENUATION
import jp.kai.forcelayout.COULOMB
import jp.kai.forcelayout.Forcelayout
import jp.kai.forcelayout.Links
import jp.kai.forcelayout.NodeBaseInfo
import java.util.ArrayList
import java.util.Random
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by kai on 2017/05/01.
 * Builder Class
 */

class ForceProperty(
  private val mContext: Context,
  val forcelayout: Forcelayout
) {
  var isReady: Boolean = false

  var lock = ReentrantLock()

  /** node's and link's List */
  internal var nodes = emptyList<Node>()
  internal var edges = emptyList<Edge>()

  /** draw area */
  var displayWidth: Float = 0f
  var displayHeight: Float = 0f
//  private var drawAreaWidth: Float = 0f
  /** draw-area means screen-size */
//  private var drawAreaHeight: Float = 0f

  /** spring-like force */
  private var distance: Int = 300
  private var bounce: Double = 0.08
  private var gravity: Double = 0.08

  /** node style */
  private var reduction: Int = 30

  internal fun prepare(): ForceProperty {
    val wm = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val point = Point()

    display.getSize(point)
    displayWidth = forcelayout.measuredWidth.toFloat()
    displayHeight = forcelayout.measuredHeight.toFloat()
    isReady = false

    return this
  }

  @Synchronized
  fun nodes(nodemaps: List<NodeBaseInfo>): ForceProperty {

    isReady = false

    initNodes()

    val tempNodes = mutableListOf<Node>()

    for (i in nodemaps.indices) {

      val nodeBaseInfo = nodemaps[i]

      var radius = displayWidth

      radius = when (nodeBaseInfo.level) {
        0 -> {
          dip2px(mContext, 30f).toFloat()
        }
        1 -> {
          dip2px(mContext, 25f).toFloat()
        }
        2 -> {
          dip2px(mContext, 20f).toFloat()
        }

        else -> {
          dip2px(mContext, 15f).toFloat()
        }
      }
      //半径转化为直径
      radius *= 2

      var x = 0f
      var y = 0f

      if (i == 0) {
        x = displayWidth / 2.0f
        y = displayHeight / 2.0f
      } else {
        val random = Random(System.currentTimeMillis())
        var n = if (random.nextBoolean()) {
          -1
        } else {
          1
        }
        x = displayWidth / 2.0f + n * radius + random.nextInt(20)
        y = displayHeight / 2.0f + n * radius + random.nextInt(20)
      }

      var tempNode = addNode(nodeBaseInfo, radius.toInt(), radius.toInt(), x, y)
      tempNodes.add(tempNode)

    }
    nodes = tempNodes
    return this
  }

  private val ADJUST = 0.5f

  /**
   * 将dip或dp值转换为px值，保证尺寸大小不变
   * @param dipValue
   * dip的值
   * @param context
   * 上下文
   * @return dip的px值
   */
  fun dip2px(
    context: Context,
    dipValue: Float
  ): Int { // scale（DisplayMetrics类中属性density）
    val scale = context.resources
        .displayMetrics
        .density
    return (dipValue * scale + ADJUST).toInt()
  }

  @Synchronized
  fun links(linkMaps: Links): ForceProperty {
    isReady = false
    initEdges()

    val tempEdges = mutableListOf<Edge>()

    /**
     * 构造邻接矩阵
     */
    for (i in nodes.indices) {
      for (j in nodes.indices) {
        val e = Edge()
        e.from = i
        e.to = j
        e.group = false
        tempEdges.add(e)
      }
    }

    val nodeSize = nodes.size



    for (k in 0 until linkMaps.size) {
      val pair = linkMaps[k]
      val parent = pair.parent()
      val child = pair.child()
      tempEdges[parent.nodexIndex + nodeSize * child.nodexIndex].group = true
      tempEdges[child.nodexIndex + nodeSize * parent.nodexIndex].group = true

    }

    edges = tempEdges
    return this
  }

  fun friction(bounce: Double): ForceProperty {
    this.bounce = bounce

    return this
  }

  fun distance(distance: Int): ForceProperty {
    this.distance = distance

    return this
  }

  fun gravity(gravity: Double): ForceProperty {
    this.gravity = gravity

    return this
  }

  @Synchronized
  fun start() {

    //注意这处代码放入协助体的话请注意线程安全问题
    val tempNodes = nodes
    val tempEdges = edges

    repeat(250) {
      var setp = 1
      for (i in tempNodes.indices step setp) {
        relax(i, i + 1, tempNodes, tempEdges)

        repeat(1) { relaxEdge(i, i + 1, tempNodes, tempEdges) }
      }
    }

    isReady = true
    calcLayoutMinAndMax()

  }

  private fun calcLayoutMinAndMax() {
    nodes.firstOrNull()
        ?.let { node ->
          forcelayout.nodeMinLeft = node
          forcelayout.nodeMaxRight = node
          forcelayout.nodeMinTop = node
          forcelayout.nodeMaxBottom = node
        }

    var currentLeft = forcelayout.nodeMinLeft ?: return
    var currentRight = forcelayout.nodeMaxRight ?: return
    var currentTop = forcelayout.nodeMinTop ?: return
    var currentBottom = forcelayout.nodeMaxBottom ?: return

    for ((index, node) in nodes.withIndex()) {
      if (currentLeft.x > node.x) {
        currentLeft = node
      }
      if (currentRight.x < node.x) {
        currentRight = node
      }
      if (currentTop.y > node.y) {
        currentTop = node
      }
      if (currentBottom.y < node.y) {
        currentBottom = node
      }

    }
    forcelayout.nodeMinLeft = currentLeft
    forcelayout.nodeMaxRight = currentRight
    forcelayout.nodeMinTop = currentTop
    forcelayout.nodeMaxBottom = currentBottom

  }

  private fun addNode(
    baseInfo: NodeBaseInfo,
    width: Int,
    height: Int,
    x: Float,
    y: Float

  ): Node {
    Random(System.currentTimeMillis()).nextInt()
    val n = Node()
    n.x = x.toDouble()
    n.y = y.toDouble()
    n.nodename = baseInfo.title
    n.width = width.toDouble()
    n.height = height.toDouble()
    n.dx = 0.0
    n.dy = 0.0
    n.id = baseInfo.id

    n.level = baseInfo.level
    n.parentPosition = baseInfo.parentPositon
    n.colorArray = baseInfo.colorArray
    return n

  }

  private fun relaxEdge(
    calc: Int,
    endCalc: Int,
    tempNodes: List<Node>,
    tempEdges: List<Edge>
  ) {

    for (i in calc until endCalc) {

      var fx = 0.0
      var fy = 0.0
      if (i >= tempNodes.size) {
        return
      }

      val calcNode = tempNodes.getOrNull(i) ?: return

      if (i == 0) {
        calcNode.x = displayWidth / 2.0
        calcNode.y = displayHeight / 2.0
        break
      }

      for (j in tempNodes.indices) {
        var distX = 0.0
        var distY = 0.0

        val edge = tempEdges[i + j * tempNodes.size]
        if (edge.group) {

          if (i == edge.from) {
            distX = tempNodes[edge.to].x - calcNode.x
            distY = tempNodes[edge.to].y - calcNode.y

          } else if (i == edge.to) {
            distX = tempNodes[edge.from].x - calcNode.x
            distY = tempNodes[edge.from].y - calcNode.y
          }

          fx += bounce * distX
          fy += bounce * distY
        }

      }

      for (j in tempNodes.indices) {
        var distX = 0.0
        var distY = 0.0
        if (tempEdges[j + i * tempNodes.size].group) {
          if (i == tempEdges[j + i * tempNodes.size].from) {
            distX = tempNodes[tempEdges[j + i * tempNodes.size].to].x - calcNode.x
            distY = tempNodes[tempEdges[j + i * tempNodes.size].to].y - calcNode.y

          } else if (i == edges[j + i * tempNodes.size].to) {
            distX = tempNodes[tempEdges[j + i * tempNodes.size].from].x - calcNode.x
            distY = tempNodes[tempEdges[j + i * tempNodes.size].from].y - calcNode.y
          }

//          bounce=0.05//高分辨率
//          bounce=0.08 //低分辨率应该大一些
          fx += bounce * distX
          fy += bounce * distY

        }

      }


      calcNode.dx = (calcNode.dx + fx) * ATTENUATION
      calcNode.dy = (calcNode.dy + fy) * ATTENUATION

      if (!calcNode.dx.isFinite()) {
        calcNode.dx = 0.0
      }
      if (!calcNode.dy.isFinite()) {
        calcNode.dy = 0.0
      }
      calcNode.x += calcNode.dx
      calcNode.y += calcNode.dy
    }

  }

  /**
   * D3算法，计算碰撞
   */
  private fun relax(
    calc: Int,
    endCalc: Int,
    tempNodes: List<Node>,
    tempEdges: List<Edge>
  ) {

    for (i in calc until endCalc) {

      var fx = 0.0
      var fy = 0.0
      if (i >= tempNodes.size) {
        return
      }

      val calcNode = tempNodes.getOrNull(i) ?: return

      if (i == 0) {
        calcNode.x = displayWidth / 2.0
        calcNode.y = displayHeight / 2.0
        break
      }

      for (j in tempNodes.indices) {
        tempNodes.getOrNull(j) ?: return
        val distX =
          ((calcNode.x + calcNode.width / 2 - (tempNodes[j].x + tempNodes[j].width / 2)).toInt()).toDouble()
        val distY =
          ((calcNode.y + calcNode.height / 2 - (tempNodes[j].y + tempNodes[j].height / 2)).toInt()).toDouble()
        var rsq = distX * distX + distY * distY
        val rsq_round = rsq.toInt() * 100
        rsq = (rsq_round / 100).toDouble()

        var coulombDistX = COULOMB * distX
        var coulombDistY = COULOMB * distY
        val coulombDistRoundX = coulombDistX.toInt() * 100
        val coulombDistRoundY = coulombDistY.toInt() * 100
        coulombDistX = (coulombDistRoundX / 100).toDouble()
        coulombDistY = (coulombDistRoundY / 100).toDouble()

        if (rsq != 0.0 && Math.sqrt(rsq) < distance) {
          fx += coulombDistX / rsq
          fy += coulombDistY / rsq
        }
      }

      /** calculate gravity */
      val distXC = displayWidth / 2 - (calcNode.x + calcNode.width / 2)
      val distYC = displayHeight / 2 - (calcNode.y + calcNode.height / 2)

//      gravity=0.08
      fx += gravity * distXC
      fy += gravity * distYC


      calcNode.dx = (calcNode.dx + fx) * ATTENUATION
      calcNode.dy = (calcNode.dy + fy) * ATTENUATION

      if (!calcNode.dx.isFinite()) {
        calcNode.dx = 0.0
      }
      if (!calcNode.dy.isFinite()) {
        calcNode.dy = 0.0
      }
      calcNode.x += calcNode.dx
      calcNode.y += calcNode.dy

    }

  }

  private fun initNodes() {
    nodes = emptyList()

  }

  private fun initEdges() {
    edges = ArrayList()
  }
}
