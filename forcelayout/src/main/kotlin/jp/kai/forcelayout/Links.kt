package jp.kai.forcelayout

import jp.kai.forcelayout.Links.LinkPair
import java.util.ArrayList

/**
 * Created by kai on 2017/05/12.
 */

class Links : ArrayList<LinkPair>() {

    class LinkPair(
        private val parent: NodeBaseInfo,
        private val child: NodeBaseInfo
    ) {

        fun parent(): NodeBaseInfo {
            return parent
        }

        fun child(): NodeBaseInfo {
            return child
        }
    }
}