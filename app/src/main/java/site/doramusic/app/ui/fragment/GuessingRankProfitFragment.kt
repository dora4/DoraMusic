package site.doramusic.app.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import dora.BaseFragment
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import site.doramusic.app.R
import site.doramusic.app.databinding.FragmentGuessingRankProfitBinding
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.ui.adapter.GuessingRankAdapter

class GuessingRankProfitFragment : BaseFragment<FragmentGuessingRankProfitBinding>() {

    private val adapter = GuessingRankAdapter(1)

    override fun getLayoutId(): Int {
        return R.layout.fragment_guessing_rank_profit
    }

    override fun initData(savedInstanceState: Bundle?, binding: FragmentGuessingRankProfitBinding) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@GuessingRankProfitFragment.adapter
        }
        loadList()
    }

    private fun loadList() {
        net {
            val data = result(GuessingService::class) { getRank(1) }?.data
            if (data?.isNotEmpty() == true) {
                adapter.setList(data)
                mBinding.emptyLayout.showContent()
            } else {
                mBinding.emptyLayout.showEmpty()
            }
        }
    }
}