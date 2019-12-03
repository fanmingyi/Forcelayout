package cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties

import jp.kai.forcelayout.NodeBaseInfo

class NodeColorHelper {

  companion object {

    /**
     * 用于在画圆的时候使用shader的渐变色的前色
     */
    private val topColors = mutableListOf(
        0xFFFDC021.toInt(),
        0xFF46A8F2.toInt(),
        0xFFFF7781.toInt(),
        0xFFE64FE7.toInt(),
        0xFF33EA90.toInt(),
        0xFFE9C989.toInt(),
        0xFF09FFA6.toInt(),
        0xFFF49AFF.toInt(),
        0xFFFFE78F.toInt(),
        0xFF49A1E9.toInt(),
        0xFFA495FF.toInt(),
        0xFF00D56D.toInt()
    )

    /**
     * 用于在画圆的时候使用shader的渐变色的后色
     */
    private val bottomColor = mutableListOf(
        0xFFFB8C0E.toInt(),
        0xFF216FE1.toInt(),
        0xFFFF424A.toInt(),
        0xFFC827CA.toInt(),
        0xFF17D057.toInt(),
        0xFFCE9851.toInt(),
        0xFF09FFA6.toInt(),
        0xFFE561FF.toInt(),
        0xFFFDD236.toInt(),
        0xFF2368CE.toInt(),
        0xFF6B5CFF.toInt(),
        0xFF00AA3B.toInt()
    )

    fun setShaderByLevel(
      nodeInfo: NodeBaseInfo,
      parentPosition: Int,
      parentInfo: NodeBaseInfo

    ) {

      try {
        nodeInfo.colorArray = when (nodeInfo.level) {
          //level为0固定颜色
          0 -> {
            intArrayOf(topColors[0], bottomColor[0])
          }
          //level为1的时候取
          1 -> {
            val topIndex = ((parentPosition) % topColors.size).coerceAtLeast(1)
            val bottomIndex = ((parentPosition) % topColors.size).coerceAtLeast(1)
            intArrayOf(
                topColors[topIndex],
                bottomColor[bottomIndex]
            )

          }
          else -> {
            parentInfo.colorArray.copyOf(parentInfo.colorArray.size)
          }
        }
      } catch (e: Exception) {
        //保证不会越界，虽然逻辑不可能发生，但是有时候复制数组少的时候就傻逼了
        e.printStackTrace()
      }
    }
  }
}