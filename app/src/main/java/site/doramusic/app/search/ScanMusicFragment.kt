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
import site.doramusic.app.conf.AppConfig
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
        private const val MAX_SELECT_COUNT = AppConfig.MUSIC_LIST_MAX_LIST
    }

    private lateinit var radarView: DoraRadarView

    private lateinit var etSearch: EditText

    private lateinit var tvStatus: TextView

    private lateinit var tvSelected: TextView

    private lateinit var tvAdd: TextView

    private lateinit var tvSelectAll: TextView

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
     * 注意：
     *
     * 扫描出来的 Music 尚未插入数据库，
     * 因此不能使用 Music.id 作为唯一 Key。
     *
     * 使用 data，也就是歌曲文件绝对路径作为 Key。
     */
    private val selectedSongs = LinkedHashMap<String, Music>()

    private lateinit var helper: PermissionHelper

    override fun getLayoutId(): Int {
        return R.layout.fragment_scan_music
    }

    override fun initData(
        savedInstanceState: Bundle?,
        binding: FragmentScanMusicBinding
    ) {
        helper = PermissionHelper
            .with(this)
            .prepare(
                PermissionHelper.Permission.READ_MEDIA_AUDIO,
                PermissionHelper.Permission.READ_EXTERNAL_STORAGE,
                PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE
            )
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
        if (!isAdded) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /**
             * Android 13+。
             */
            val granted = helper.hasPermission(
                requireActivity(),
                PermissionHelper.Permission.READ_MEDIA_AUDIO
            )
            if (granted) {
                startScan()
                return
            }
            helper.permissions(
                    PermissionHelper.Permission.READ_MEDIA_AUDIO
                )
                .request { grantedResult ->
                    if (grantedResult) {
                        startScan()
                    } else {
                        tvStatus.text = "没有音乐读取权限"
                    }
                }
            return
        }

        /**
         * Android 12 及以下。
         */
        if (PermissionHelper.hasStoragePermission(requireActivity())) {
            startScan()
            return
        }

        /**
         * Android 11 / 12。
         *
         * 如果 PermissionHelper 的普通权限请求无法处理，
         * 跳系统设置。
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tvStatus.text = "请开启存储权限"
            startActivity(
                IntentUtils.getRequestStoragePermissionIntent(
                    requireActivity().packageName
                )
            )
            return
        }

        /**
         * Android 10 及以下。
         */
        helper
            .permissions(
                PermissionHelper.Permission.READ_EXTERNAL_STORAGE,
                PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE
            )
            .request { grantedResult ->

                if (grantedResult) {
                    startScan()
                } else {
                    tvStatus.text = "没有存储读取权限"
                }
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
        tvSelectAll = binding.tvSelectAll
        adapter = MusicResultAdapter(
            list = displaySongs,
            selectedIds = selectedSongs.keys,
            listener = object :
                MusicResultAdapter.Listener {

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
     * 初始化监听器。
     */
    private fun initListeners() {
        /**
         * 全选 / 取消当前列表。
         */
        tvSelectAll.setOnClickListener {
            selectAllSongs()
        }
        /**
         * 重新扫描。
         */
        mBinding.btnScan.setOnClickListener {
            if (!mBinding.btnScan.isEnabled) {
                return@setOnClickListener
            }
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
     *
     * 保证扫描动画至少持续1秒。
     */
    private fun startScan() {
        if (!isAdded) {
            return
        }
        /**
         * 记录扫描开始时间。
         */
        val startTime = System.currentTimeMillis()
        radarView.visibility = View.VISIBLE
        radarView.start()
        radarView.setCenterText("扫描中")
        tvStatus.text = "正在扫描本地歌曲..."
        mBinding.btnScan.isEnabled = false
        lifecycleScope.launch {
            try {
                /**
                 * 真正的 MediaStore 扫描放到 IO 线程。
                 */
                val result = withContext(Dispatchers.IO) {
                    MusicScanner.scanMediaStore(requireContext())
                }
                /**
                 * 计算扫描已经耗时多久。
                 */
                val elapsed = System.currentTimeMillis() - startTime
                /**
                 * 至少显示1秒。
                 */
                val remainTime = 1000L - elapsed
                if (remainTime > 0) {
                    kotlinx.coroutines.delay(remainTime)
                }
                /**
                 * 保存扫描结果。
                 */
                allSongs.clear()
                allSongs.addAll(
                    result.filter {
                        !it.data.isNullOrBlank()
                    }
                )
                /**
                 * 当前显示列表恢复为全部歌曲。
                 */
                displaySongs.clear()
                displaySongs.addAll(allSongs)
                /**
                 * 重新扫描后，
                 * 清除已经不存在的选择。
                 */
                val validPaths = allSongs
                        .mapNotNull {
                            it.data
                        }
                        .toHashSet()
                selectedSongs.keys
                    .toList()
                    .forEach { key ->
                        if (!validPaths.contains(key)) {
                            selectedSongs.remove(key)
                        }
                    }
                /**
                 * 刷新列表。
                 */
                adapter.notifyDataSetChanged()
                /**
                 * 停止扫描动画。
                 */
                radarView.stop()
                radarView.visibility = View.GONE
                /**
                 * 恢复扫描按钮。
                 */
                mBinding.btnScan.isEnabled = true
                /**
                 * 更新状态。
                 */
                tvStatus.text = "共找到 ${allSongs.size} 首歌曲"
                updateSelectedUI()
            } catch (e: Exception) {
                e.printStackTrace()
                /**
                 * 即使扫描异常，也保证动画至少显示1秒。
                 */
                val elapsed = System.currentTimeMillis() - startTime
                val remainTime = 1000L - elapsed
                if (remainTime > 0) {
                    kotlinx.coroutines.delay(remainTime)
                }
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
    private fun filterSongs(keyword: String?) {
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
                    buildSongSearchText(song)
                        .lowercase(Locale.getDefault())
                        .contains(text)
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
        updateSelectAllUI()
    }

    /**
     * 构造歌曲完整搜索文本。
     */
    private fun buildSongSearchText(song: Music): String {
        return buildString {
            append(song.musicName ?: "")
            append(" ")
            append(song.artist ?: "")
        }
    }

    /**
     * 选择 / 取消选择。
     *
     * 使用歌曲文件路径 data 作为唯一 Key。
     */
    private fun toggleSong(song: Music) {
        val key = song.data
        /**
         * 没有文件路径无法进行唯一识别。
         */
        if (key.isNullOrBlank()) {
            return
        }
        if (selectedSongs.containsKey(key)) {
            /**
             * 已选择 -> 取消。
             */
            selectedSongs.remove(key)
        } else {
            /**
             * 未选择 -> 添加。
             */
            if (selectedSongs.size >= MAX_SELECT_COUNT) {
                showShortToast(
                    "最多选择 $MAX_SELECT_COUNT 首歌曲"
                )
                return
            }
            selectedSongs[key] = song
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
        tvAdd.isEnabled = count > 0
        updateSelectAllUI()
    }

    /**
     * 更新全选按钮状态。
     *
     * 当前搜索结果全部选中：
     *     取消全选
     *
     * 否则：
     *     全部选择
     */
    private fun updateSelectAllUI() {
        if (displaySongs.isEmpty()) {
            tvSelectAll.text = "全部选择"
            return
        }
        val validSongs =
            displaySongs.filter {
                !it.data.isNullOrBlank()
            }
        if (validSongs.isEmpty()) {
            tvSelectAll.text = "全部选择"
            return
        }
        val allSelected = validSongs.all { song ->
                selectedSongs.containsKey(
                    song.data
                )
            }
        tvSelectAll.text =
            if (allSelected) {
                "取消全选"
            } else {
                "全部选择"
            }
    }

    /**
     * 将选中的歌曲保存到本地数据库。
     */
    private fun addSelectedSongs() {
        if (selectedSongs.isEmpty()) {
            return
        }
        /**
         * 拷贝一份。
         *
         * 避免异步过程中 selectedSongs 被修改。
         */
        val songs = selectedSongs.values.toList()
        tvAdd.isEnabled = false
        lifecycleScope.launch {
            try {
                /**
                 * 数据库操作放到 IO。
                 */
                val addedCount =
                    withContext(Dispatchers.IO) {
                        MusicScanner.addSelectedSongs(
                            requireContext(),
                            songs
                        )
                    }
                /**
                 * 保存成功后清空选择。
                 */
                selectedSongs.clear()
                updateSelectedUI()
                adapter.notifyDataSetChanged()
                /**
                 * 通知 Home 刷新。
                 */
                RxBus.getInstance()
                    .post(
                        RefreshHomeItemEvent()
                    )
                if (addedCount == songs.size) {
                    showShortToast(
                        "已添加 $addedCount 首歌曲"
                    )

                } else if (addedCount > 0) {
                    showShortToast(
                        "已添加 $addedCount 首歌曲，" +
                                "其中 ${songs.size - addedCount} 首已存在"
                    )

                } else {
                    showShortToast(
                        "所选歌曲已经添加"
                    )
                }
                requireActivity().finish()
            } catch (e: Exception) {
                e.printStackTrace()
                showShortToast(
                    "添加歌曲失败：" +
                            "${e.message ?: "未知错误"}"
                )
            } finally {
                /**
                 * 如果 Fragment 还存在，
                 * 根据当前选择状态恢复按钮。
                 */
                if (isAdded) {
                    tvAdd.isEnabled =
                        selectedSongs.isNotEmpty()
                }
            }
        }
    }

    /**
     * 全选 / 取消全选当前显示的歌曲。
     *
     * 规则：
     *
     * 1. 只操作 displaySongs。
     * 2. selectedSongs 保存全局选择状态。
     * 3. 当前列表全部已选 -> 取消当前列表。
     * 4. 当前列表存在未选 -> 选择当前列表。
     * 5. 搜索状态下，只操作搜索结果。
     */
    private fun selectAllSongs() {
        if (displaySongs.isEmpty()) {
            showShortToast(
                "没有可添加的歌曲"
            )
            return
        }
        val validSongs = displaySongs.filter {
                !it.data.isNullOrBlank()
            }
        if (validSongs.isEmpty()) {
            showShortToast(
                "没有可添加的歌曲"
            )
            return
        }
        /**
         * 当前显示列表是否全部选中。
         */
        val allSelected =
            validSongs.all { song ->
                selectedSongs.containsKey(
                    song.data
                )
            }
        if (allSelected) {
            /**
             * 当前列表全部取消。
             *
             * 如果正在搜索，
             * 只取消当前搜索结果。
             */
            validSongs.forEach { song ->
                song.data?.let { key ->
                    selectedSongs.remove(key)
                }
            }
        } else {
            /**
             * 当前列表全部选择。
             */
            var reachedLimit = false
            for (song in validSongs) {
                val key = song.data ?: continue
                /**
                 * 已经选择。
                 */
                if (selectedSongs.containsKey(key)) {
                    continue
                }
                /**
                 * 达到最大选择数量。
                 */
                if (selectedSongs.size >= MAX_SELECT_COUNT) {
                    reachedLimit = true
                    break
                }
                selectedSongs[key] = song
            }
            if (reachedLimit) {
                showShortToast(
                    "最多选择 $MAX_SELECT_COUNT 首歌曲"
                )
            }
        }
        updateSelectedUI()
        adapter.notifyDataSetChanged()
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