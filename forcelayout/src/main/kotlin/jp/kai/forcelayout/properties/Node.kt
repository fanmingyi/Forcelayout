package cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties

import android.graphics.Color
import jp.kai.forcelayout.NodeBaseInfo
import java.util.UUID

/**
 * Created by kai on 2017/05/03.
 * Node Class
 *
 * Update by fmy on 2019-11-27
 *
 */

public class Node {
  var attachData: Any?=null

  var textColor: Int = NodeBaseInfo.defaultTextColor
  var textSize: Int = 28

  var nodename: String = ""

  var x: Double = 0.toDouble()
  var y: Double = 0.toDouble()
  var width: Double = 0.toDouble()
  var height: Double = 0.toDouble()

  var dx: Double = 0.toDouble()
  var dy: Double = 0.toDouble()
  var level: Int = 0
  var id: String = UUID.randomUUID()
      .toString()

  //在父节点的下标
  var parentPosition: Int = 0
  var colorArray: IntArray = intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT)

  /**
   * 返回一个数组长度为2的颜色数组
   * 可以用完成渐变色
   */
  fun getGradientColor(): IntArray {
    return colorArray
  }

}