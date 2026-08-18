package site.doramusic.app.search

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dora.BaseFragment
import dora.util.IntentUtils
import dora.util.PermissionHelper
import dora.util.RxBus
import dora.widget.DoraRadarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import site.doramusic.app.R
import site.doramusic.app.databinding.FragmentScanMusicBinding
import site.doramusic.app.db.Music
import site.doramusic.app.event.RefreshHomeItemEvent
import site.doramusic.app.media.MusicScanner
import site.doramusic.app.util.ThemeSelector
import java.util.Locale

/**
 * 本地歌曲搜索 / 批量选择页面。
 */
class ScanMusicFragment : BaseFragment<FragmentScanMusicBinding>() {

    companion object {

        /**
         * 最大选择数量。
         */
        private const val MAX_SELECT_COUNT = 1000
    }

    private lateinit var radarView: DoraRadarView

    private lateinit var etSearch: EditText

    private lateinit var tvStatus: TextView

    private lateinit var tvSelected: TextView

    private lateinit var tvAdd: TextView

    private lateinit var tvSelectAll: TextView

    private lateinit var tvCancelAll: TextView

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: MusicResultAdapter

    /**
     * MusicScanner 扫描出来的全部歌曲。
     */
    private val allSongs = mutableListOf<Music>()

    /**
     * 当前搜索结果。
     */
    private val displaySongs = mutableListOf<Music>()

    /**
     * 当前选中的歌曲。
     *
     * Key 使用 Music.id。
     */
    private val selectedSongs = LinkedHashMap<Int, Music>()

    private lateinit var helper: PermissionHelper

    override fun getLayoutId(): Int {
        return R.layout.fragment_scan_music
    }

    override fun initData(
        savedInstanceState: Bundle?,
        binding: FragmentScanMusicBinding
    ) {
        helper = PermissionHelper.with(this).prepare(
            PermissionHelper.Permission.READ_MEDIA_AUDIO,
            PermissionHelper.Permission.READ_EXTERNAL_STORAGE,
            PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE)
        initViews(binding)
        initListeners()
        requestMusicPermission()
    }

    /**
     * 请求本地音乐读取权限。
     *
     * Android 13+：
     *     READ_MEDIA_AUDIO
     *
     * Android 12 及以下：
     *     READ_EXTERNAL_STORAGE
     */
    private fun requestMusicPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /**
             * Android 13+
             *
             * 优先使用 READ_MEDIA_AUDIO。
             */
            val granted = helper.hasPermission(requireActivity(),
                PermissionHelper.Permission.READ_MEDIA_AUDIO)
            if (granted) {
                startScan()
                return
            }
            helper.permissions(PermissionHelper.Permission.READ_MEDIA_AUDIO).request {
                startScan()
            }
        } else {
            /**
             * Android 12 及以下：
             * 使用 READ_EXTERNAL_STORAGE。
             */
            if (!PermissionHelper.hasStoragePermission(requireActivity())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(IntentUtils.getRequestStoragePermissionIntent(requireActivity().packageName))
                } else {
                    helper.permissions(PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE).request {
                        if (it) {
                            startScan()
                        }
                    }
                }
            } else {
                startScan()
            }
        }
    }

    private fun initViews(
        binding: FragmentScanMusicBinding
    ) {
        radarView = binding.radarView
        etSearch = binding.etSearch
        tvStatus = binding.tvStatus
        tvSelected = binding.tvSelected
        tvAdd = binding.tvAdd
        recyclerView = binding.recyclerView
        tvSelectAll = binding.tvSelectAll
        tvCancelAll = binding.tvCancelAll
        adapter = MusicResultAdapter(
            list = displaySongs,
            selectedIds = selectedSongs.keys,
            listener = object : MusicResultAdapter.Listener {

                override fun onChecked(song: Music) {
                    toggleSong(song)
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        radarView.setRadarColor(
            ThemeSelector.getThemeColor(
                requireContext()
            )
        )
        radarView.setCenterText("扫描")
        updateSelectedUI()
    }

    private fun initListeners() {
        /**
         * 全部添加。
         *
         * 按当前显示列表进行全选。
         * 如果当前正在搜索，则只选择搜索结果。
         */
        tvSelectAll.setOnClickListener {
            selectAllSongs()
        }
        /**
         * 全部取消。
         */
        tvCancelAll.setOnClickListener {
            clearAllSelectedSongs()
        }
        /**
         * 重新扫描。
         */
        mBinding.btnScan.setOnClickListener {
            startScan()
        }
        /**
         * 添加。
         */
        tvAdd.setOnClickListener {
            addSelectedSongs()
        }
        /**
         * 搜索。
         */
        etSearch.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    filterSongs(s?.toString())
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    /**
     * 开始扫描本地歌曲。
     */
    private fun startScan() {
        radarView.visibility = View.VISIBLE
        radarView.start()
        radarView.setCenterText("扫描中")
        tvStatus.text = "正在扫描本地歌曲..."
        mBinding.btnScan.isEnabled = false
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    MusicScanner.scanMediaStore(
                        requireContext()
                    )
                }
                allSongs.clear()
                allSongs.addAll(result)
                displaySongs.clear()
                displaySongs.addAll(result)
                adapter.notifyDataSetChanged()
                radarView.stop()
                radarView.visibility = View.GONE
                mBinding.btnScan.isEnabled = true
                tvStatus.text = "共找到 ${result.size} 首歌曲"
                updateSelectedUI()
            } catch (e: Exception) {
                radarView.stop()
                radarView.visibility = View.GONE
                mBinding.btnScan.isEnabled = true
                tvStatus.text = "扫描失败"
                showShortToast(
                    "扫描歌曲失败：${e.message ?: "未知错误"}"
                )
            }
        }
    }

    /**
     * 根据关键字过滤歌曲。
     */
    private fun filterSongs(
        keyword: String?
    ) {
        val text = keyword
                ?.trim()
                ?.lowercase(Locale.getDefault())
                ?: ""
        displaySongs.clear()
        if (text.isEmpty()) {
            displaySongs.addAll(allSongs)
        } else {
            displaySongs.addAll(
                allSongs.filter { song ->
                    song.musicName
                        ?.lowercase(Locale.getDefault())
                        ?.contains(text) == true ||
                            song.artist
                                ?.lowercase(Locale.getDefault())
                                ?.contains(text) == true ||
                            song.data
                                ?.lowercase(Locale.getDefault())
                                ?.contains(text) == true
                }
            )
        }
        adapter.notifyDataSetChanged()
        tvStatus.text = if (text.isEmpty()) {
                "共找到 ${allSongs.size} 首歌曲"
            } else {
                "显示 ${displaySongs.size} / ${allSongs.size} 首歌曲"
            }
    }

    /**
     * 选择 / 取消选择。
     */
    private fun toggleSong(
        song: Music
    ) {
        val id = song.id
        if (selectedSongs.containsKey(id)) {
            /**
             * 已选择 -> 取消。
             */
            selectedSongs.remove(id)
        } else {
            if (selectedSongs.size >= MAX_SELECT_COUNT) {
                showShortToast(
                    "最多选择 $MAX_SELECT_COUNT 首歌曲"
                )
                return
            }
            selectedSongs[id] = song
        }
        updateSelectedUI()
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新底部选择状态。
     */
    private fun updateSelectedUI() {
        val count = selectedSongs.size
        tvSelected.text = "已选择 $count / $MAX_SELECT_COUNT"
        tvAdd.text =
            if (count == 0) {
                "添加到我的音乐"
            } else {
                "添加 $count 首"
            }
        tvAdd.alpha =
            if (count == 0) {
                0.5f
            } else {
                1f
            }
        tvAdd.isEnabled =
            count > 0
    }

    /**
     * 将选中的歌曲保存到本地数据库。
     */
    private fun addSelectedSongs() {
        if (selectedSongs.isEmpty()) {
            return
        }
        val songs = selectedSongs.values.toList()
        tvAdd.isEnabled = false
        lifecycleScope.launch {
            try {
                /**
                 * 数据库操作放到 IO。
                 */
                val addedCount = withContext(Dispatchers.IO) {
                    MusicScanner.addSelectedSongs(
                        requireContext(),
                        songs
                    )
                }
                /**
                 * 清空选择。
                 */
                selectedSongs.clear()
                updateSelectedUI()
                adapter.notifyDataSetChanged()
                /**
                 * 通知 Home 刷新。
                 */
                RxBus.getInstance().post(RefreshHomeItemEvent())
                if (addedCount == songs.size) {
                    showShortToast("已添加 $addedCount 首歌曲")
                } else if (addedCount > 0) {
                    showShortToast("已添加 $addedCount 首歌曲，其中 ${songs.size - 
                            addedCount} 首已存在")
                } else {
                    showShortToast("所选歌曲已经添加")
                }
                requireActivity().finish()
            } catch (e: Exception) {
                showShortToast("添加歌曲失败：${e.message ?: "未知错误"}")
            } finally {
                tvAdd.isEnabled = selectedSongs.isNotEmpty()
            }
        }
    }

    /**
     * 全部选择当前显示的歌曲。
     *
     * 注意：
     * 使用 displaySongs，而不是 allSongs。
     *
     * 所以搜索后点击「全部添加」，
     * 只会选择当前搜索结果。
     */
    private fun selectAllSongs() {
        if (displaySongs.isEmpty()) {
            showShortToast("没有可添加的歌曲")
            return
        }
        if (selectedSongs.size >= MAX_SELECT_COUNT) {
            showShortToast(
                "最多选择 $MAX_SELECT_COUNT 首歌曲"
            )
            return
        }
        var addedCount = 0
        for (song in displaySongs) {
            /**
             * 已经选择的歌曲跳过。
             */
            if (selectedSongs.containsKey(song.id)) {
                continue
            }
            /**
             * 达到最大选择数量。
             */
            if (selectedSongs.size >= MAX_SELECT_COUNT) {
                break
            }
            selectedSongs[song.id] = song
            addedCount++
        }
        updateSelectedUI()
        adapter.notifyDataSetChanged()
        if (addedCount == 0) {
            showShortToast("当前歌曲已经全部选择")
        } else if (selectedSongs.size >= MAX_SELECT_COUNT) {
            showShortToast(
                "已选择 $MAX_SELECT_COUNT 首歌曲"
            )
        }
    }

    /**
     * 取消全部选择。
     */
    private fun clearAllSelectedSongs() {
        if (selectedSongs.isEmpty()) {
            return
        }
        selectedSongs.clear()
        updateSelectedUI()
        adapter.notifyDataSetChanged()
    }
}