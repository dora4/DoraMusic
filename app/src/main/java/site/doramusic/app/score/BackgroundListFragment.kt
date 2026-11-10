package site.doramusic.app.score

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import dora.BaseFragment
import site.doramusic.app.R
import site.doramusic.app.databinding.FragmentBgListBinding
import site.doramusic.app.util.PadUtils

class BackgroundListFragment(
    private val bgList: MutableList<PlayerBackground>,
    private val onBgClick: (PlayerBackground) -> Unit
) : BaseFragment<FragmentBgListBinding>() {

    var adapter: PlayerBackgroundAdapter? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_bg_list
    }

    override fun initData(savedInstanceState: Bundle?, binding: FragmentBgListBinding) {
        val spanCount = if (PadUtils.isPadLandscape(requireActivity())) 4 else 2
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        adapter = PlayerBackgroundAdapter(requireContext(), bgList)
        adapter?.setOnItemClickListener { _, _, pos ->
            val item = adapter?.getItem(pos) ?: return@setOnItemClickListener
            onBgClick.invoke(item)
        }
        binding.recyclerView.adapter = adapter
    }

    fun updateSelectId(newId: String) {
        adapter?.setSelectedBackground(newId)
    }
}
