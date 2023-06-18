package site.doramusic.app.sort

import java.util.*

/**
 * 拼音比较器，用于列表排序。
 */
class PinyinComparator : Comparator<Sort> {

    override fun compare(lhs: Sort, rhs: Sort): Int {
        return if (lhs.sortLetter == "↑" || rhs.sortLetter == "#") {
            -1
        } else if (lhs.sortLetter == "#" || rhs.sortLetter == "↑") {
            1
        } else {
            lhs.sortLetter.compareTo(rhs.sortLetter)
        }
    }
}