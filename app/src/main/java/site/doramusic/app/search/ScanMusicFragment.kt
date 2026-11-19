package site.doramusic.app.search

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import dora.brvah.decoration.DividerItemDecoration
import dora.db.dao.DaoFactory
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
import site.doramusic.app.track.EventType
import site.doramusic.app.track.TrackAnalysis
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
     *      * 扫描出来的 Music 尚未插入数据库，
     *      * 因此不能使用 Music.id 作为唯一 Key。
     *      * 使用 data，也就是歌曲文件绝对路径作为 Key。
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
        helper = PermissionHelper.with(this)
            .prepare(
                PermissionHelper.Permission.READ_MEDIA_AUDIO,
                PermissionHelper.Permission.READ_EXTERNAL_STORAGE,
                PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE
            )
        initViews(binding)
        initListeners()
        requestMusicPermission()
    }

    override fun onResume() {
        super.onResume()
        if (!isAdded) return
        // Android11+ 从系统设置页返回，自动校验权限并扫描
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && PermissionHelper.hasStoragePermission(requireActivity())
        ) {
            startScan()
        }
    }

    /**
     * 请求本地音乐读取权限。
     *
     * Android 13+：
     *     READ_MEDIA_AUDIO
     *
     * Android 11 ~ Android12：
     *     MANAGE_EXTERNAL_STORAGE（全部文件访问，只能跳系统设置）
     *
     * Android 10 及以下：
     *     READ_EXTERNAL_STORAGE
     */
    private fun requestMusicPermission() {
        if (!isAdded) {
            return
        }
        when {
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
//                val audioPerm = PermissionHelper.Permission.READ_MEDIA_AUDIO
//                if (helper.hasPermission(requireActivity(), audioPerm)) {
//                    startScan()
//                    return
//                }
//                helper.relativePermissions(audioPerm)
//                    .request { grantedResult ->
//                        if (!isAdded) return@request
//                        if (grantedResult) {
//                            startScan()
//                        } else {
//                            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_AUDIO)) {
//                                tvStatus.text = getString(R.string.no_read_music_permission)
//                                gotoAppSetting()
//                            } else {
//                                tvStatus.text = getString(R.string.no_read_music_permission)
//                            }
//                        }
//                    }
//            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android11 ~ Android12
                if (PermissionHelper.hasStoragePermission(requireActivity())) {
                    startScan()
                    return
                }
                tvStatus.text = getString(R.string.storage_permission_prompt)
                startActivity(
                    IntentUtils.getRequestStoragePermissionIntent(
                        requireActivity().packageName
                    )
                )
            }
            else -> {
                // Android10及以下
                val readStorage = PermissionHelper.Permission.READ_EXTERNAL_STORAGE
                val writeStorage = PermissionHelper.Permission.WRITE_EXTERNAL_STORAGE
                if (helper.hasPermission(requireActivity(), readStorage)) {
                    startScan()
                    return
                }
                helper.permissions(readStorage, writeStorage)
                    .request { grantedResult ->
                        if (!isAdded) return@request
                        if (grantedResult) {
                            startScan()
                        } else {
                            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                tvStatus.text = getString(R.string.storage_permission_prompt)
                                gotoAppSetting()
                            } else {
                                tvStatus.text = getString(R.string.storage_permission_prompt)
                            }
                        }
                    }
            }
        }
    }

    /**
     * 跳转应用详情设置页，用于权限永久拒绝场景。
     */
    private fun gotoAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }

    /**
     * 初始化 View。
     */
    private fun initViews(binding: FragmentScanMusicBinding) {
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
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(),
            DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        radarView.setRadarColor(
            ThemeSelector.getThemeColor(
                requireContext()
            )
        )
        radarView.setCenterText(ContextCompat.getString(requireContext(), R.string.scan))
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
            TrackAnalysis.report(
                lifecycleScope,
                EventType.EVENT_TYPE_SCAN_MUSIC
            )
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
        radarView.setCenterText(getString(R.string.scaning))
        tvStatus.text = getString(R.string.now_scaning_local_music)
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
                if (!isAdded) return@launch
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
                val count = allSongs.size
                tvStatus.text = resources.getQuantityString(R.plurals.found_music_template, count, count)
                updateSelectedUI()
            } catch (e: Exception) {
                /**
                 * 即使扫描异常，也保证动画至少显示1秒。
                 */
                val elapsed = System.currentTimeMillis() - startTime
                val remainTime = 1000L - elapsed
                if (remainTime > 0) {
                    kotlinx.coroutines.delay(remainTime)
                }
                if (!isAdded) return@launch
                radarView.stop()
                radarView.visibility = View.GONE
                mBinding.btnScan.isEnabled = true
                tvStatus.text = getString(R.string.failed_to_scan)
                showShortToast(
                    getString(R.string.scan_music_error_template, e.message ?: getString(R.string.unknown_error))
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
                val count = allSongs.size
                resources.getQuantityString(R.plurals.found_music_template, count, count)
            } else {
                getString(R.string.show_music_template, displaySongs.size, allSongs.size)
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
            append(" ")
            append(song.albumName ?: "")
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
                val count = MAX_SELECT_COUNT
                val tipText = resources.getQuantityString(R.plurals.select_music_limit_template, count, count)
                showShortToast(tipText)
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
        tvSelected.text = getString(R.string.selected_template, count, MAX_SELECT_COUNT)
        tvAdd.text =
            if (count == 0) {
                getString(R.string.added_to_my_music)
            } else {
                resources.getQuantityString(R.plurals.add_template, count, count)
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
     * 否则：
     *     全部选择
     */
    private fun updateSelectAllUI() {
        if (displaySongs.isEmpty()) {
            tvSelectAll.text = getString(R.string.select_all)
            return
        }
        val validSongs =
            displaySongs.filter {
                !it.data.isNullOrBlank()
            }
        if (validSongs.isEmpty()) {
            tvSelectAll.text = getString(R.string.select_all)
            return
        }
        val allSelected = validSongs.all { song ->
            selectedSongs.containsKey(
                song.data
            )
        }
        tvSelectAll.text =
            if (allSelected) {
                getString(R.string.cancel_select_all)
            } else {
                getString(R.string.select_all)
            }
    }

    /**
     * 添加结果，用于区分提示文案。
     */
    private data class AddResult(
        val added: Int,
        val dup: Int,
        val dropped: Int,
        val quotaFull: Boolean
    )

    /**
     * 将选中的歌曲保存到本地数据库。
     *
     * 勾选阶段不限制数量，落库时以“本地已有 + 本次去重后的新歌”为准：
     *      1. 先读取本地已有歌曲，建立去重指纹集合；
     *      2. 把本次勾选拆成「新歌」和「重复」两组；
     *      3. 只对「新歌」按剩余配额截断，补齐到上限，其余丢弃；
     *      4. 因此即使唯一一首新歌排在勾选列表末尾，也不会被前面的重复项挤掉。
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
                 *
                 * 先查本地已有数量与指纹，算出还能补多少首，
                 * 再对“去重后的新歌”做截断，保证不破上限且不误删新歌。
                 */
                val result = withContext(Dispatchers.IO) {
                    val dao = DaoFactory.getDao(Music::class.java)
                    @Suppress("UNCHECKED_CAST")
                    val localList: List<Music> = dao.selectAll()
                    val localFingerprints = localList.mapNotNull { it.fingerprint() }.toSet()
                    val localPaths = localList
                        .mapNotNull { it.data?.trim()?.lowercase(Locale.getDefault()) }
                        .toSet()
                    val localCount = localList.size
                    val quota = (MAX_SELECT_COUNT - localCount).coerceAtLeast(0)
                    if (quota <= 0) {
                        AddResult(added = 0, dup = 0, dropped = 0, quotaFull = true)
                    } else {
                        val seenInBatch = HashSet<String>()
                        val newSongs = mutableListOf<Music>()
                        var dupCount = 0
                        for (song in songs) {
                            val fp = song.fingerprint()
                            val path = song.data?.trim()?.lowercase(Locale.getDefault()).orEmpty()
                            val duplicated =
                                (fp != null && localFingerprints.contains(fp)) ||
                                        localPaths.contains(path) ||
                                        (fp != null && seenInBatch.contains(fp))
                            if (duplicated) {
                                dupCount++
                            } else {
                                fp?.let { seenInBatch.add(it) }
                                newSongs.add(song)
                            }
                        }
                        // 只对“新歌”按剩余配额截断，
                        val toAdd = newSongs.take(quota)
                        val added = MusicScanner.addSelectedSongs(toAdd)
                        AddResult(
                            added = added,
                            dup = dupCount,
                            dropped = (newSongs.size - added).coerceAtLeast(0),
                            quotaFull = false
                        )
                    }
                }
                if (!isAdded) return@launch
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
                TrackAnalysis.report(lifecycleScope, EventType.EVENT_TYPE_ADD_MUSIC,
                    mapOf("count" to result.added)
                )
                showAddResultToast(result, songs.size)
                requireActivity().finish()
            } catch (e: Exception) {
                if (!isAdded) return@launch
                showShortToast(
                    getString(R.string.scan_music_error_template, e.message ?: getString(R.string.unknown_error))
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
     * 根据添加结果给出差异化 Toast。
     */
    private fun showAddResultToast(result: AddResult, selectedTotal: Int) {
        when {
            result.quotaFull -> {
                showShortToast(getString(R.string.selected_music_already_added))
            }
            result.added == selectedTotal -> {
                // 全部加进去：无重复、无超配额
                showShortToast(
                    resources.getQuantityString(
                        R.plurals.added_music_template, result.added, result.added
                    )
                )
            }
            result.added > 0 -> {
                // 部分成功：added 首入库，其余是重复或超配额
                showShortToast(
                    resources.getQuantityString(
                        R.plurals.added_music_merge_template,
                        result.added,        // 用于判断单复数（plurals quantity）
                        result.added,        // 填充 %1$d
                        selectedTotal - result.added  // 填充 %2$d
                    )
                )
            }
            else -> {
                // 一首没加：全是重复
                showShortToast(getString(R.string.selected_music_already_added))
            }
        }
    }

    /**
     * 全选 / 取消全选当前显示的歌曲。
     *
     * 规则：
     * 1. 只操作 displaySongs。
     * 2. selectedSongs 保存全局选择状态。
     * 3. 当前列表全部已选 -> 取消当前列表。
     * 4. 当前列表存在未选 -> 选择当前列表。
     * 5. 搜索状态下，只操作搜索结果。
     */
    private fun selectAllSongs() {
        if (displaySongs.isEmpty()) {
            showShortToast(
                getString(R.string.no_available_music_to_add)
            )
            return
        }
        val validSongs = displaySongs.filter {
            !it.data.isNullOrBlank()
        }
        if (validSongs.isEmpty()) {
            showShortToast(
                getString(R.string.no_available_music_to_add)
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
                val tipText = resources.getQuantityString(R.plurals.select_music_limit_template,
                    MAX_SELECT_COUNT, MAX_SELECT_COUNT)
                showShortToast(tipText)
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