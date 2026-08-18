package site.doramusic.app.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dora.BaseFragment
import dora.util.RxBus
import dora.widget.DoraRadarView
import dora.widget.DoraTitleBar
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
 *
 * MusicScanner 负责：
 *
 * 1. MediaStore 扫描
 * 2. 创建 Music
 * 3. 写入 Music 数据库
 * 4. 收藏迁移
 * 5. Artist / Album / Folder 数据同步
 *
 * 本页面只负责：
 *
 * 1. 展示歌曲
 * 2. 搜索
 * 3. 多选
 * 4. 通知 Home 刷新
 */
class ScanMusicFragment : BaseFragment<FragmentScanMusicBinding>() {

    companion object {

        /**
         * 最大选择数量。
         */
        private const val MAX_SELECT_COUNT = 1000

        /**
         * 音乐读取权限。
         */
        private const val REQUEST_READ_MUSIC = 10001
    }

    private lateinit var radarView: DoraRadarView

    private lateinit var etSearch: EditText

    private lateinit var tvStatus: TextView

    private lateinit var tvSelected: TextView

    private lateinit var tvAdd: TextView

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

    override fun getLayoutId(): Int {
        return R.layout.fragment_scan_music
    }

    override fun initData(
        savedInstanceState: Bundle?,
        binding: FragmentScanMusicBinding
    ) {
        initViews(binding)
        initListeners()
        if (hasMusicPermission()) {
            startScan()
        } else {
            requestMusicPermission()
        }
    }

    /**
     * 初始化 View。
     */
    private fun initViews(
        binding: FragmentScanMusicBinding
    ) {
        radarView = binding.radarView
        etSearch = binding.etSearch
        tvStatus = binding.tvStatus
        tvSelected = binding.tvSelected
        tvAdd = binding.tvAdd
        recyclerView = binding.recyclerView

        adapter = MusicResultAdapter(
            list = displaySongs,
            selectedIds = selectedSongs.keys,
            listener = object : MusicResultAdapter.Listener {

                override fun onChecked(song: Music) {
                    toggleSong(song)
                }
            }
        )

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        radarView.setRadarColor(
            ThemeSelector.getThemeColor(
                requireContext()
            )
        )

        radarView.setCenterText("扫描")

        updateSelectedUI()
    }

    /**
     * 初始化监听。
     */
    private fun initListeners() {
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
     * 使用 MusicScanner 扫描。
     *
     * 进入页面后自动调用。
     *
     * 扫描过程中显示雷达，
     * 扫描完成后隐藏雷达。
     */
    /**
     * 开始扫描本地歌曲。
     *
     * 进入页面自动执行。
     *
     * 注意：
     * scanMediaStore() 是耗时操作，
     * 必须放到 Dispatchers.IO。
     *
     * UI 更新全部回到主线程。
     */
    private fun startScan() {

        if (!hasMusicPermission()) {
            requestMusicPermission()
            return
        }

        /**
         * 开始扫描。
         */
        radarView.visibility = View.VISIBLE
        radarView.start()
        radarView.setCenterText("扫描中")

        tvStatus.text = "正在扫描本地歌曲..."

        mBinding.btnScan.isEnabled = false

        lifecycleScope.launch {

            try {

                /**
                 * 真正的 MediaStore 查询放到 IO 线程。
                 *
                 * scanMediaStore()：
                 *
                 * 1. 查询 MediaStore
                 * 2. 创建 Music
                 * 3. 生成拼音
                 * 4. 返回 List<Music>
                 *
                 * 不操作 UI。
                 */
                val result = withContext(Dispatchers.IO) {
                    MusicScanner.scanMediaStore(
                        requireContext()
                    )
                }

                /**
                 * 回到主线程。
                 *
                 * 更新歌曲列表。
                 */
                allSongs.clear()
                allSongs.addAll(result)

                displaySongs.clear()
                displaySongs.addAll(result)

                adapter.notifyDataSetChanged()

                /**
                 * 扫描完成。
                 */
                radarView.stop()
                radarView.visibility = View.GONE

                mBinding.btnScan.isEnabled = true

                tvStatus.text =
                    "共找到 ${result.size} 首歌曲"

                updateSelectedUI()

            } catch (e: Exception) {

                e.printStackTrace()

                /**
                 * 即使扫描失败，
                 * 也必须停止雷达。
                 */
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
            displaySongs.addAll(
                allSongs
            )
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
        tvStatus.text =
            if (text.isEmpty()) {
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

    private fun addSelectedSongs() {
        if (selectedSongs.isEmpty()) {
            return
        }
        val count = selectedSongs.size
        tvAdd.isEnabled = false
        lifecycleScope.launch {
            selectedSongs.clear()
            updateSelectedUI()
            adapter.notifyDataSetChanged()
            /**
             * 通知 Home 刷新。
             */
            RxBus.getInstance().post(RefreshHomeItemEvent())
            showShortToast(
                "已添加 $count 首歌曲"
            )
            tvAdd.isEnabled = false
        }
    }

    /**
     * 是否拥有音乐读取权限。
     */
    private fun hasMusicPermission(): Boolean {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 请求音乐读取权限。
     */
    private fun requestMusicPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO
                ),
                REQUEST_READ_MUSIC
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_READ_MUSIC
            )
        }
    }

    /**
     * 权限回调。
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
        if (requestCode != REQUEST_READ_MUSIC) {
            return
        }
        if (grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startScan()
        } else {
            showShortToast(
                "需要音乐读取权限才能扫描本地歌曲"
            )
        }
    }
}