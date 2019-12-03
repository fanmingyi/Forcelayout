package jp.kai.forcelayout

import android.graphics.Color
import java.util.UUID

data class NodeBaseInfo(
  var title: String,
  val level: Int,
  val nodexIndex: Int,
  var colorArray: IntArray = intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT),
  val parentPositon: Int = 0,
  val id: String = UUID.randomUUID().toString(),
  var textColor: Int = Int.MAX_VALUE,
  var textSize: Int = Int.MAX_VALUE
) {
  init {
    if (textSize == Int.MAX_VALUE) {
      setDefaultTextSize(this)
    }
    if (textColor == Int.MAX_VALUE) {
      setDefaultTextColor(this)
    }
  }

  companion object {
    val defaultTextColor by lazy {
      Color.GREEN
    }

    fun setDefaultTextColor(baseInfo: NodeBaseInfo) {
      baseInfo.textColor = when (baseInfo.level) {
        else -> {
          defaultTextColor
        }
      }
    }

    fun setDefaultTextSize(baseInfo: NodeBaseInfo) {
      baseInfo.textSize = when (baseInfo.level) {
        else -> {
         26
        }
      }

    }

  }
}