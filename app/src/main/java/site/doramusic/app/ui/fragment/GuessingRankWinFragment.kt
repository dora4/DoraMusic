package site.doramusic.app.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import dora.BaseFragment
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import site.doramusic.app.R
import site.doramusic.app.databinding.FragmentGuessingRankWinBinding
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.ui.adapter.GuessingRankAdapter

class GuessingRankWinFragment : BaseFragment<FragmentGuessingRankWinBinding>() {

    private val adapter = GuessingRankAdapter(0)

    override fun getLayoutId(): Int {
        return R.layout.fragment_guessing_rank_win
    }

    override fun initData(savedInstanceState: Bundle?, binding: FragmentGuessingRankWinBinding) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@GuessingRankWinFragment.adapter
        }
        loadList()
    }

    private fun loadList() {
        net {
            val data = result(GuessingService::class) { getRank(0) }?.data
            if (data?.isNotEmpty() == true) {
                adapter.setList(data)
                mBinding.emptyLayout.showContent()
            } else {
                mBinding.emptyLayout.showEmpty()
            }
        }
    }
}