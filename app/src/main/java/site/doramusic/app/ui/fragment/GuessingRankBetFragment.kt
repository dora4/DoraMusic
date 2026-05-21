package site.doramusic.app.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import dora.BaseFragment
import dora.http.DoraHttp.net
import dora.http.DoraHttp.result
import site.doramusic.app.R
import site.doramusic.app.databinding.FragmentGuessingRankBetBinding
import site.doramusic.app.http.service.GuessingService
import site.doramusic.app.ui.adapter.GuessingRankAdapter

class GuessingRankBetFragment : BaseFragment<FragmentGuessingRankBetBinding>() {

    private val adapter = GuessingRankAdapter(2)

    override fun getLayoutId(): Int {
        return R.layout.fragment_guessing_rank_bet
    }

    override fun initData(savedInstanceState: Bundle?, binding: FragmentGuessingRankBetBinding) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@GuessingRankBetFragment.adapter
        }
        loadList()
    }

    private fun loadList() {
        net {
            val data = result(GuessingService::class) { getRank(2) }?.data
            if (data?.isNotEmpty() == true) {
                adapter.setList(data)
                mBinding.emptyLayout.showContent()
            } else {
                mBinding.emptyLayout.showEmpty()
            }
        }
    }
}