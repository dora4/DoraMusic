package site.doramusic.app.ui.adapter

import android.widget.SectionIndexer
import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import dora.util.PinyinUtils
import dora.util.TextUtils
import site.doramusic.app.sort.PinyinComparator
import site.doramusic.app.sort.Sort
import java.util.*

abstract class BaseSortItemAdapter<T : Sort> : BaseQuickAdapter<T, BaseViewHolder>, SectionIndexer {

    private var comparator: PinyinComparator
    private var needSort = true

    constructor(@LayoutRes layoutResId: Int) : super(layoutResId) {
        this.comparator = PinyinComparator()
    }

    constructor(@LayoutRes layoutResId: Int, list: MutableList<T>) : super(layoutResId, list) {
        this.comparator = PinyinComparator()
        setList(list)
    }

    fun setNeedSort(needSort: Boolean) {
        this.needSort = needSort
    }

    private fun sort() {
        data.sortBy { it.sortLetter }
    }

    final override fun setList(list: Collection<T>?) {
        val letters = arrayListOf<T>()
        if (list != null) {
            letters.addAll(list)
        }
        if (needSort) {
            super.setList(generateLetters(letters))
            sort()
        } else {
            super.setList(list)
        }
    }

    protected abstract fun getSortKey(data: T): String

    private fun generateLetters(list: MutableList<T>): MutableList<T> {
        for (bean in list) {
            val sortKey = getSortKey(bean)
            val sortLetter = PinyinUtils.getPinyinFromSentence(sortKey).uppercase(Locale.ENGLISH)
            bean.sortLetter = sortLetter
        }
        return list
    }

    override fun getSections(): Array<Any> {
        return arrayOf(0)
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        for (i in 0 until itemCount) {
            val sortLetter = data[i].sortLetter
            if (sortLetter != null) {
                val first = sortLetter.uppercase(Locale.ENGLISH)[0]
                if (first.code == sectionIndex) {
                    return i
                }
            }
        }
        return -1
    }

    override fun getSectionForPosition(pos: Int): Int {
        return if (getItem(pos).sortLetter == null)
            -1
        else
            getItem(pos).sortLetter.uppercase(Locale.ENGLISH)[0].code
    }

    fun getPositionForSection(sectionIndex: Char): Int {
        for (i in 0 until itemCount) {
            val sortLetter = data[i].sortLetter
            if (TextUtils.isNotEmpty(sortLetter)) {
                val first = sortLetter.uppercase(Locale.ENGLISH)[0]
                if (first == sectionIndex) {
                    return i
                }
            }
        }
        return -1
    }
}
