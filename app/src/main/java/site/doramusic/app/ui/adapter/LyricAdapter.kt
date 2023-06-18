package site.doramusic.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.lwh.jackknife.xskin.SkinLoader
import site.doramusic.app.R
import site.doramusic.app.lrc.LyricLine
import site.doramusic.app.util.PreferencesManager
import java.util.*

class LyricAdapter(internal var context: Context) : BaseAdapter() {

    /**
     * 歌词句子集合。
     */
    private var lyricLines: MutableList<LyricLine>? = null

    /**
     * 当前的句子索引号。
     */
    private var indexOfCurrentSentence: Int = 0

    /**
     * 20sp。
     */
    private var currentSize = 20f

    /**
     * 17sp。
     */
    private var notCurrentSize = 17f

    init {
        lyricLines = ArrayList()
        indexOfCurrentSentence = 0
    }

    override fun isEmpty(): Boolean {
        return if (lyricLines == null) {
            true
        } else lyricLines!!.size === 0
    }

    override fun getCount(): Int {
        return lyricLines!!.size
    }

    /**
     * 设置歌词，由外部调用，
     */
    fun setLyric(lyric: MutableList<LyricLine>?) {
        lyricLines!!.clear()
        if (lyric != null) {
            lyricLines!!.addAll(lyric)
        }
        indexOfCurrentSentence = 0
    }

    override fun isEnabled(position: Int): Boolean {
        // 禁止在列表条目上点击
        return false
    }

    fun clear() {
        lyricLines!!.clear()
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Any {
        return lyricLines!![position].contentText
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_lyric_line, null)
            holder.lyric_line = convertView!!.findViewById(R.id.tv_lyric_line)
            convertView!!.tag = holder
        } else {
            holder = convertView!!.tag as ViewHolder
        }
        if (position >= 0 && position < lyricLines!!.size) {
            holder.lyric_line!!.text = lyricLines!![position]
                .contentText
        }
        if (indexOfCurrentSentence == position) {
            val prefsManager = PreferencesManager(context)
            val skinType = prefsManager!!.getSkinType()
            // 当前播放到的句子设置为白色，字体大小更大
            if (skinType == 0) {
                val skinColor = prefsManager!!.getSkinColor()
                holder.lyric_line!!.setTextColor(context.resources.getColor(skinColor))
            } else {
                val skinThemeColor = SkinLoader.getInstance().getColorRes("skin_theme_color")
                holder.lyric_line!!.setTextColor(context.resources.getColor(skinThemeColor))
            }
            holder.lyric_line!!.textSize = currentSize
        } else {
            // 其他的句子设置为暗色，字体大小较小
            holder.lyric_line!!.setTextColor(
                context.resources.getColor(
                    R.color.trans_white
                )
            )
            holder.lyric_line!!.textSize = notCurrentSize
        }
        return convertView
    }

    fun setCurrentSentenceIndex(index: Int) {
        indexOfCurrentSentence = index
    }

    internal class ViewHolder {
        var lyric_line: TextView? = null
    }
}
