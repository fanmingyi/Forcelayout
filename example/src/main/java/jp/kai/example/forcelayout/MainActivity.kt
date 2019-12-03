package jp.kai.example.forcelayout

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import cn.jingzhuan.stock.topic.industrychain.charview.forcelayout.properties.NodeColorHelper
import jp.kai.forcelayout.COULOMB
import jp.kai.forcelayout.Forcelayout
import jp.kai.forcelayout.Links
import jp.kai.forcelayout.Links.LinkPair
import jp.kai.forcelayout.NodeBaseInfo

/**
 * Created by kai on 2016/09/03.
 * Usage for Kotlin
 */

class MainActivity : Activity() {
  private var isFlip: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val force: Forcelayout = findViewById<Forcelayout>(R.id.forcelayout) as Forcelayout
    example2(force)

    val seekBar: SeekBar? = findViewById(R.id.seekbar)



    seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(
        seekBar: SeekBar,
        progress: Int,
        fromUser: Boolean
      ) {
        force.with()
            .gravity((progress.toFloat() / 1000).toDouble())
            .start()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })
  }

  private fun example2(force: Forcelayout) {

    var nodesBaseInfoList: MutableList<NodeBaseInfo> = mutableListOf()
    /** set links */
    val links: Links = Links()

    //一级

    var firstTitle = "国企改革#1"

    val firstNodeBaseInfo = NodeBaseInfo(firstTitle, 0, parentPositon = 0,nodexIndex = nodesBaseInfoList.size)
    NodeColorHelper.setShaderByLevel(firstNodeBaseInfo, 0, firstNodeBaseInfo)
    nodesBaseInfoList.add(firstNodeBaseInfo)

    for (i in 0..5) {
      val seondTitle = "${i}级标签#2"
      //创建二级

      val secondeNodeBaseInfo = NodeBaseInfo(seondTitle, 1, parentPositon = i,nodexIndex =  nodesBaseInfoList.size)
      nodesBaseInfoList.add(secondeNodeBaseInfo)
      NodeColorHelper.setShaderByLevel(secondeNodeBaseInfo, i, secondeNodeBaseInfo)
      //二级链接
      links.add(LinkPair(firstNodeBaseInfo, secondeNodeBaseInfo))

      for (j in 0..5) {
//        //创建三级
        var thirdTitle = "${i}标签:#3---${j}"
        val thirNodeBaseInfo = NodeBaseInfo(thirdTitle, 2, parentPositon = j,nodexIndex =  nodesBaseInfoList.size)
        nodesBaseInfoList.add(thirNodeBaseInfo)
        NodeColorHelper.setShaderByLevel(thirNodeBaseInfo, j, secondeNodeBaseInfo)
        links.add(LinkPair(secondeNodeBaseInfo, thirNodeBaseInfo))


      }
    }



    force.link()
        .style(Color.argb(60, 50, 30, 200), 5.0f)

    COULOMB=dip2px(this,680f).toDouble()
    force.with()
        .distance(dip2px(this,500f))
        /** distance between each nodes */ //距离中心点的权重，越大里中心点越近
        .gravity(0.05)
        /** gravitation from center of view */ //摩擦系数 越大每个node越远
        .friction(0.05)
        /** value of gravitation between each nodes */
        .nodes(nodesBaseInfoList)
        /** set nodes */
        .links(links)
        /** set links */
        .start()
    /** start animation */
  }

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
    return (dipValue * scale + 0.5).toInt()
  }
}