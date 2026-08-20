package site.doramusic.app.media

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dora.db.Orm
import dora.db.Transaction
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.table.TableManager
import dora.util.PinyinUtils
import dora.util.TextUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import site.doramusic.app.conf.AppConfig
import site.doramusic.app.db.Album
import site.doramusic.app.db.Artist
import site.doramusic.app.db.Folder
import site.doramusic.app.db.Music
import site.doramusic.app.util.MusicUtils
import site.doramusic.app.util.PrefsManager
import java.io.File
import kotlin.collections.ArrayList

/**
 * 媒体扫描器，用来扫描手机中的歌曲文件。
 *
 * 说明：
 *
 * MediaStore：
 *     MediaStore.Audio.Media._ID
 *              ↓
 *          Music.songId
 *
 * 本地数据库：
 *     Music.id
 *
 * 文件唯一标识：
 *     Music.data
 *
 * 因此：
 *
 * 1. songId 是 MediaStore 的歌曲 ID
 * 2. id 是本地数据库主键
 * 3. data 是本地文件路径，也是添加歌曲时的去重依据
 *
 * 扫描页面选择歌曲时，不应该使用 Music.id。
 */
@SuppressLint("Range")
object MusicScanner : AppConfig {

    /**
     * MediaStore 音乐查询字段。
     */
    private val projMusic = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.DURATION
    )

    /**
     * MediaStore 专辑查询字段。
     */
    private val projAlbum = arrayOf(
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.NUMBER_OF_SONGS,
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM_ART
    )

    /**
     * MediaStore 歌手查询字段。
     */
    private val projArtist = arrayOf(
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    )

    /**
     * MediaStore 文件夹查询字段。
     */
    private val projFolder = arrayOf(
        MediaStore.Files.FileColumns.DATA
    )

    /**
     * 防止没有 WHERE 条件时 SQL 拼接异常。
     */
    private const val DEFENSE_SQL_INJECTION_HEADER = " 1=1 "

    /**
     * DAO。
     */
    private val musicDao = DaoFactory.getDao(Music::class.java)

    private val artistDao = DaoFactory.getDao(Artist::class.java)

    private val albumDao = DaoFactory.getDao(Album::class.java)

    private val folderDao = DaoFactory.getDao(Folder::class.java)

    /**
     * 重建所有音乐相关表。
     *
     * 注意：
     * 只在完整扫描 scan() 中使用。
     */
    private fun recreateTables() {
        Transaction.execute {
            TableManager.recreateTable(Music::class.java)
            TableManager.recreateTable(Artist::class.java)
            TableManager.recreateTable(Album::class.java)
            TableManager.recreateTable(Folder::class.java)
        }
    }

    // ============================================================
    // 完整扫描
    // ============================================================

    /**
     * 完整扫描本地音乐，并重建音乐相关数据库。
     *
     * 这个方法会：
     *
     * 1. 保存旧收藏状态
     * 2. 重建 Music / Artist / Album / Folder 表
     * 3. 从 MediaStore 重新扫描
     * 4. 根据 data 恢复收藏
     * 5. 保存歌曲
     * 6. 保存歌手
     * 7. 保存专辑
     * 8. 保存文件夹
     */
    @JvmStatic
    fun scan(context: Context): Observable<List<Music>> {
        return Observable.fromCallable {

            /**
             * 1. 保存旧收藏。
             *
             * 使用 data 文件路径作为唯一标识，
             * 因为数据库 id 在重建表之后会发生变化。
             */
            val oldFavorites = musicDao.select(
                WhereBuilder
                    .create()
                    .addWhereEqualTo(
                        Music.COLUMN_FAVORITE,
                        Music.IS_FAVORITE
                    )
            )

            val favoriteMap = oldFavorites
                .filter {
                    !it.data.isNullOrBlank()
                }
                .associateBy {
                    it.data
                }

            /**
             * 2. 重建表。
             */
            try {
                recreateTables()
            } catch (ignored: Exception) {
                // 保持原有容错逻辑
            }

            var musics: List<Music> = emptyList()

            /**
             * 3. 扫描并插入 Music。
             */
            Transaction.execute(Music::class.java) {
                musics = queryMusic(
                    context,
                    AppConfig.ROUTE_START_FROM_LOCAL
                )

                /**
                 * 4. 恢复收藏状态。
                 */
                musics.forEach { music ->
                    val path = music.data
                    if (!path.isNullOrBlank() &&
                        favoriteMap.containsKey(path)
                    ) {
                        music.favorite = Music.IS_FAVORITE
                    }
                }

                /**
                 * 5. 插入音乐。
                 */
                if (musics.isNotEmpty()) {
                    it.insert(musics)
                }
            }

            /**
             * 6. 保存 Artist / Album / Folder。
             */
            if (musics.isNotEmpty()) {
                Transaction.execute {
                    val artists = queryArtist(context)
                    if (artists.isNotEmpty()) {
                        artistDao.insert(artists)
                    }
                    val albums = queryAlbum(context)
                    if (albums.isNotEmpty()) {
                        albumDao.insert(albums)
                    }
                    val folders = queryFolder(context)
                    if (folders.isNotEmpty()) {
                        folderDao.insert(folders)
                    }
                }
            }
            musics
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // ============================================================
    // Music 查询
    // ============================================================

    @JvmStatic
    fun queryMusic(
        context: Context,
        from: Int
    ): List<Music> {
        return queryMusic(
            context,
            null,
            null,
            from
        )
    }

    /**
     * 根据入口查询音乐。
     */
    @JvmStatic
    fun queryMusic(
        context: Context,
        selections: String?,
        selection: String?,
        from: Int
    ): List<Music> {

        val sp = PrefsManager(context)

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cr = context.contentResolver

        val select = StringBuilder(
            DEFENSE_SQL_INJECTION_HEADER
        )

        /**
         * 文件大小过滤。
         */
        if (sp.getFilterSize()) {
            select.append(
                " AND ${MediaStore.Audio.Media.SIZE} > " +
                        AppConfig.SCANNER_FILTER_SIZE
            )
        }

        /**
         * 时长过滤。
         */
        if (sp.getFilterTime()) {
            select.append(
                " AND ${MediaStore.Audio.Media.DURATION} > " +
                        AppConfig.SCANNER_FILTER_DURATION
            )
        }

        /**
         * 自定义 SQL 条件。
         */
        if (TextUtils.isNotEmpty(selections)) {
            select.append(selections)
        }

        return when (from) {

            /**
             * 本地音乐。
             */
            AppConfig.ROUTE_START_FROM_LOCAL -> {
                if (musicDao.count() > 0) {
                    musicDao.selectAll()
                } else {
                    getMusicList(
                        cr.query(
                            uri,
                            projMusic,
                            select.toString(),
                            null,
                            MediaStore.Audio.Media.ARTIST_KEY
                        )
                    )
                }
            }

            /**
             * 歌手。
             */
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                if (musicDao.count() > 0) {
                    queryMusic(
                        selection,
                        AppConfig.ROUTE_START_FROM_ARTIST
                    )
                } else {
                    getMusicList(
                        cr.query(
                            uri,
                            projMusic,
                            select.toString(),
                            null,
                            MediaStore.Audio.Media.ARTIST_KEY
                        )
                    )
                }
            }

            /**
             * 专辑。
             */
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                if (musicDao.count() > 0) {
                    queryMusic(
                        selection,
                        AppConfig.ROUTE_START_FROM_ALBUM
                    )
                } else {
                    getMusicList(
                        cr.query(
                            uri,
                            projMusic,
                            select.toString(),
                            null,
                            MediaStore.Audio.Media.ALBUM_KEY
                        )
                    )
                }
            }

            /**
             * 文件夹。
             */
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                if (musicDao.count() > 0) {
                    queryMusic(
                        selection,
                        AppConfig.ROUTE_START_FROM_FOLDER
                    )
                } else {
                    getMusicList(
                        cr.query(
                            uri,
                            projMusic,
                            select.toString(),
                            null,
                            MediaStore.Audio.Media.DATA
                        )
                    )
                }
            }

            /**
             * 收藏。
             */
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                if (musicDao.count() > 0) {
                    queryMusic(
                        selection,
                        AppConfig.ROUTE_START_FROM_FAVORITE
                    )
                } else {
                    emptyList()
                }
            }

            /**
             * 最近播放。
             */
            AppConfig.ROUTE_START_FROM_LATEST -> {
                if (musicDao.count() > 0) {
                    queryMusic(
                        selection,
                        AppConfig.ROUTE_START_FROM_LATEST
                    )
                } else {
                    emptyList()
                }
            }
            else -> {
                emptyList()
            }
        }
    }

    // ============================================================
    // Raw SQL
    // ============================================================

    @JvmStatic
    fun rawQueryMusic(
        selection: String?,
        type: Int
    ): List<Music> {
        val sql = when (type) {
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                "SELECT * FROM music " +
                        "WHERE ${Music.COLUMN_ARTIST} = ?"
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                "SELECT * FROM music " +
                        "WHERE ${Music.COLUMN_ALBUM_ID} = ?"
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                "SELECT * FROM music " +
                        "WHERE ${Music.COLUMN_FOLDER} = ?"
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                "SELECT * FROM music " +
                        "WHERE ${Music.COLUMN_FAVORITE} = ?"
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                "SELECT * FROM music " +
                        "WHERE ${Music.COLUMN_LAST_PLAY_TIME} > ? " +
                        "ORDER BY ${Music.COLUMN_LAST_PLAY_TIME} DESC " +
                        "LIMIT 100"
            }
            else -> {
                "SELECT * FROM music"
            }
        }
        return if (selection == null) {
            parseCursor(
                Orm.getDB().rawQuery(
                    sql,
                    null
                )
            )
        } else {
            parseCursor(
                Orm.getDB().rawQuery(
                    sql,
                    arrayOf(selection)
                )
            )
        }
    }

    // ============================================================
    // Cursor -> Music
    // ============================================================

    /**
     * 将数据库 Cursor 转成 Music。
     */
    private fun parseCursor(
        cursor: Cursor
    ): List<Music> {
        val list = ArrayList<Music>()
        cursor.use {
            while (it.moveToNext()) {
                val music = Music()
                music.id = it.getInt(
                    it.getColumnIndex(
                        Music.COLUMN_ID
                    )
                )
                music.songId = it.getInt(
                    it.getColumnIndex(
                        Music.COLUMN_SONG_ID
                    )
                )
                music.albumId = it.getInt(
                    it.getColumnIndex(
                        Music.COLUMN_ALBUM_ID
                    )
                )
                music.duration = it.getInt(
                    it.getColumnIndex(
                        Music.COLUMN_DURATION
                    )
                )
                music.musicName = it.getString(
                    it.getColumnIndex(
                        Music.COLUMN_MUSIC_NAME
                    )
                )
                music.artist = it.getString(
                    it.getColumnIndex(
                        Music.COLUMN_ARTIST
                    )
                )
                music.data = it.getString(
                    it.getColumnIndex(
                        Music.COLUMN_DATA
                    )
                )
                music.folder = it.getString(
                    it.getColumnIndex(
                        Music.COLUMN_FOLDER
                    )
                )
                music.musicNameKey = it.getString(
                    it.getColumnIndex(
                        Music.COLUMN_MUSIC_NAME_KEY
                    )
                )
                music.artistKey = it.getString(
                    it.getColumnIndex(
                        Music.COLUMN_ARTIST_KEY
                    )
                )
                music.favorite = it.getInt(
                    it.getColumnIndex(
                        Music.COLUMN_FAVORITE
                    )
                )
                music.lastPlayTime = it.getLong(
                    it.getColumnIndex(
                        Music.COLUMN_LAST_PLAY_TIME
                    )
                )
                list.add(music)
            }
        }
        return list
    }

    // ============================================================
    // DAO Query
    // ============================================================

    @JvmStatic
    fun queryMusic(
        value: String?,
        type: Int
    ): List<Music> {
        val queryBuilder = createQueryBuilder(
            value,
            type
        )
        return musicDao.select(
            queryBuilder
        )
    }

    private fun createQueryBuilder(
        value: String?,
        type: Int
    ): QueryBuilder {
        val whereBuilder = WhereBuilder.create()
        if (value != null) {
            when (type) {
                AppConfig.ROUTE_START_FROM_ARTIST -> {
                    whereBuilder.addWhereEqualTo(
                        Music.COLUMN_ARTIST,
                        value
                    )
                }
                AppConfig.ROUTE_START_FROM_ALBUM -> {
                    whereBuilder.addWhereEqualTo(
                        Music.COLUMN_ALBUM_ID,
                        value
                    )
                }
                AppConfig.ROUTE_START_FROM_FOLDER -> {
                    whereBuilder.addWhereEqualTo(
                        Music.COLUMN_FOLDER,
                        value
                    )
                }
                AppConfig.ROUTE_START_FROM_FAVORITE -> {
                    whereBuilder.addWhereEqualTo(
                        Music.COLUMN_FAVORITE,
                        value
                    )
                }
                AppConfig.ROUTE_START_FROM_LATEST -> {
                    whereBuilder.addWhereEqualTo(
                        Music.COLUMN_LAST_PLAY_TIME,
                        value
                    )
                }
            }
        }
        val builder = QueryBuilder
            .create()
            .where(whereBuilder)
        if (type == AppConfig.ROUTE_START_FROM_LATEST) {
            builder.orderBy(
                "-${Music.COLUMN_LAST_PLAY_TIME}"
            )
            builder.limit(100)
        }
        return builder
    }

    // ============================================================
    // Folder
    // ============================================================

    /**
     * 查询歌曲文件夹。
     */
    @JvmStatic
    fun queryFolder(
        context: Context
    ): List<Folder> {
        val sp = PrefsManager(context)
        val uri = MediaStore.Files.getContentUri(
            "external"
        )
        val cr = context.contentResolver
        val audioExtensions = listOf(
            "mp3",
            "flac",
            "wav",
            "ape",
            "m4a",
            "aac"
        )
        val likeConditions =
            audioExtensions.joinToString(" OR ") { ext ->
                "${MediaStore.Files.FileColumns.DATA} LIKE '%.$ext'"
            }
        val selection = StringBuilder().apply {
            append(
                "${MediaStore.Files.FileColumns.MEDIA_TYPE} = " +
                        "${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}"
            )
            append(" AND (")
            append(likeConditions)
            append(")")
        }
        if (sp.getFilterSize()) {
            selection.append(
                " AND ${MediaStore.Audio.Media.SIZE} > " +
                        AppConfig.SCANNER_FILTER_SIZE
            )
        }
        if (sp.getFilterTime()) {
            selection.append(
                " AND ${MediaStore.Audio.Media.DURATION} > " +
                        AppConfig.SCANNER_FILTER_DURATION
            )
        }
        return if (folderDao.count() > 0) {
            folderDao.selectAll()
        } else {
            getFolderList(
                cr.query(
                    uri,
                    projFolder,
                    selection.toString(),
                    null,
                    null
                )
            )
        }
    }

    // ============================================================
    // Artist
    // ============================================================

    /**
     * 查询歌手。
     */
    @JvmStatic
    fun queryArtist(
        context: Context
    ): List<Artist> {
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val cr = context.contentResolver
        return if (artistDao.count() > 0) {
            artistDao.selectAll()
        } else {
            getArtistList(
                cr.query(
                    uri,
                    projArtist,
                    null,
                    null,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS +
                            " DESC"
                )
            )
        }
    }

    // ============================================================
    // Album
    // ============================================================

    /**
     * 查询专辑。
     */
    @JvmStatic
    fun queryAlbum(
        context: Context
    ): List<Album> {
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val cr = context.contentResolver
        return if (albumDao.count() > 0) {
            albumDao.selectAll()
        } else {
            getAlbumList(
                cr.query(
                    uri,
                    projAlbum,
                    null,
                    null,
                    MediaStore.Audio.Media.ALBUM_KEY
                )
            )
        }
    }

    // ============================================================
    // MediaStore -> Music
    // ============================================================

    /**
     * 将 MediaStore Cursor 转成 Music。
     *
     * 重要：
     *
     * MediaStore._ID
     *      ↓
     * Music.songId
     *
     * 不设置 Music.id。
     *
     * Music.id 是本地数据库主键，
     * 等插入数据库之后再由 ORM 处理。
     */
    private fun getMusicList(
        cursor: Cursor?
    ): List<Music> {
        val list = ArrayList<Music>()
        if (cursor == null) {
            return list
        }
        cursor.use {
            while (it.moveToNext()) {
                try {
                    val music = Music()
                    // MediaStore ID
                    music.songId = it.getInt(
                        it.getColumnIndex(
                            MediaStore.Audio.Media._ID
                        )
                    )
                    // 文件路径
                    val filePath = it.getString(
                        it.getColumnIndex(
                            MediaStore.Audio.Media.DATA
                        )
                    )
                    if (filePath.isNullOrBlank()) {
                        continue
                    }
                    music.data = filePath
                    // Album
                    music.albumId = it.getInt(
                        it.getColumnIndex(
                            MediaStore.Audio.Media.ALBUM_ID
                        )
                    )
                    // 歌曲名称
                    music.musicName = it.getString(
                        it.getColumnIndex(
                            MediaStore.Audio.Media.TITLE
                        )
                    )
                    // 歌手
                    music.artist = it.getString(
                        it.getColumnIndex(
                            MediaStore.Audio.Media.ARTIST
                        )
                    )
                    // 时长
                    val duration = it.getInt(
                        it.getColumnIndex(
                            MediaStore.Audio.Media.DURATION
                        )
                    )
                    if (duration > 0) {
                        music.duration = duration
                    } else {
                        // 获取不到时不要丢弃歌曲
                        music.duration = 0
                        try {
                            music.duration =
                                MusicUtils.getDuration(filePath)
                        } catch (ignored: Exception) {
                            // 保留 0，歌曲依然加入扫描结果
                        }
                    }
                    // Folder
                    val separatorIndex =
                        filePath.lastIndexOf(File.separator)
                    music.folder =
                        if (separatorIndex > 0) {
                            filePath.substring(
                                0,
                                separatorIndex
                            )
                        } else {
                            ""
                        }
                    // 拼音
                    music.musicNameKey =
                        PinyinUtils.getPinyinFromSentence(
                            music.musicName
                        )
                    music.artistKey =
                        PinyinUtils.getPinyinFromSentence(
                            music.artist
                        )
                    list.add(music)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 单首歌曲失败，不影响其他歌曲
                }
            }
        }
        return list
    }

    // ============================================================
    // Album Cursor
    // ============================================================

    private fun getAlbumList(
        cursor: Cursor?
    ): List<Album> {
        val list = ArrayList<Album>()
        if (cursor == null) {
            return list
        }
        cursor.use {
            while (it.moveToNext()) {
                val album = Album()
                album.album_name =
                    it.getString(
                        it.getColumnIndex(
                            MediaStore.Audio.Albums.ALBUM
                        )
                    )
                album.album_id =
                    it.getInt(
                        it.getColumnIndex(
                            MediaStore.Audio.Albums._ID
                        )
                    )
                album.number_of_songs =
                    it.getInt(
                        it.getColumnIndex(
                            MediaStore.Audio.Albums.NUMBER_OF_SONGS
                        )
                    )
                album.album_cover_path =
                    it.getString(
                        it.getColumnIndex(
                            MediaStore.Audio.Albums.ALBUM_ART
                        )
                    )
                list.add(album)
            }
        }
        return list
    }

    // ============================================================
    // Artist Cursor
    // ============================================================

    private fun getArtistList(
        cursor: Cursor?
    ): List<Artist> {
        val list = ArrayList<Artist>()
        if (cursor == null) {
            return list
        }
        cursor.use {
            while (it.moveToNext()) {
                val artist = Artist()
                artist.name =
                    it.getString(
                        it.getColumnIndex(
                            MediaStore.Audio.Artists.ARTIST
                        )
                    )
                artist.number_of_tracks =
                    it.getInt(
                        it.getColumnIndex(
                            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                        )
                    )
                list.add(artist)
            }
        }
        return list
    }

    // ============================================================
    // Folder Cursor
    // ============================================================

    private fun getFolderList(
        cursor: Cursor?
    ): List<Folder> {
        val list = ArrayList<Folder>()
        if (cursor == null) {
            return list
        }
        cursor.use {
            while (it.moveToNext()) {
                val filePath =
                    it.getString(
                        it.getColumnIndex(
                            MediaStore.Files.FileColumns.DATA
                        )
                    )
                if (filePath.isNullOrBlank()) {
                    continue
                }
                val separatorIndex =
                    filePath.lastIndexOf(
                        File.separator
                    )
                if (separatorIndex <= 0) {
                    continue
                }
                val folderPath =
                    filePath.substring(
                        0,
                        separatorIndex
                    )
                val folder = Folder()
                folder.path = folderPath
                folder.name =
                    folderPath.substringAfterLast(
                        File.separator
                    )
                list.add(folder)
            }
        }
        return list
    }

    // ============================================================
    // 仅扫描 MediaStore
    // ============================================================

    /**
     * 仅扫描 MediaStore。
     *
     * 不修改数据库。
     *
     * 主要用于：
     *
     * ScanMusicFragment
     *
     * 让用户先扫描：
     *
     * MediaStore
     *      ↓
     * List<Music>
     *      ↓
     * 用户选择
     *      ↓
     * addSelectedSongs()
     */
    @JvmStatic
    fun scanMediaStore(
        context: Context
    ): List<Music> {
        val sp = PrefsManager(context)
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cr = context.contentResolver
        val select =
            StringBuilder(
                DEFENSE_SQL_INJECTION_HEADER
            )
        /**
         * 文件大小过滤。
         */
        if (sp.getFilterSize()) {
            select.append(
                " AND ${MediaStore.Audio.Media.SIZE} > " +
                        AppConfig.SCANNER_FILTER_SIZE
            )
        }

        /**
         * 时长过滤。
         */
        if (sp.getFilterTime()) {
            select.append(
                " AND ${MediaStore.Audio.Media.DURATION} > " +
                        AppConfig.SCANNER_FILTER_DURATION
            )
        }

        /**
         * 执行 MediaStore 查询。
         */
        return getMusicList(
            cr.query(
                uri,
                projMusic,
                select.toString(),
                null,
                MediaStore.Audio.Media.ARTIST_KEY
            )
        )
    }

    // ============================================================
    // 添加用户选择的歌曲
    // ============================================================

    /**
     * 将用户选择的歌曲保存到本地数据库。
     *
     * 规则：
     *
     * 1. 不重建 music 表
     * 2. 只添加用户选择的歌曲
     * 3. data 作为文件唯一标识
     * 4. 当前选择内部重复歌曲自动去重
     * 5. 同步 Artist
     * 6. 同步 Album
     * 7. 同步 Folder
     *
     * @return 实际新增歌曲数量
     */
    @JvmStatic
    fun addSelectedSongs(
        context: Context,
        songs: List<Music>
    ): Int {
        if (songs.isEmpty()) {
            return 0
        }
        var addedSongs = emptyList<Music>()
        /**
         * 插入 Music。
         */
        Transaction.execute {
            /**
             * 查询数据库中已经存在的文件路径。
             */
            val existingPaths =
                musicDao
                    .selectAll()
                    .mapNotNull {
                        it.data
                    }
                    .toHashSet()

            /**
             * 新歌曲。
             *
             * data：
             *     文件绝对路径
             *
             * 作为唯一标识。
             */
            val newSongs =
                songs
                    .filter { song ->
                        !song.data.isNullOrBlank() &&
                                !existingPaths.contains(
                                    song.data
                                )
                    }
                    .distinctBy {
                        it.data
                    }
            if (newSongs.isEmpty()) {
                return@execute
            }
            /**
             * 插入数据库。
             */
            musicDao.insert(
                newSongs
            )
            addedSongs = newSongs
        }

        if (addedSongs.isEmpty()) {
            return 0
        }

        /**
         * 查询专辑名称。
         */
        val albumNameMap =
            queryAlbumNameMap(
                context,
                addedSongs
            )

        /**
         * 同步 Artist / Album / Folder。
         */
        Transaction.execute {

            // ----------------------------------------------------
            // Artist
            // ----------------------------------------------------

            val existingArtists =
                artistDao
                    .selectAll()
                    .mapNotNull {
                        it.name
                    }
                    .toHashSet()
            val artists =
                addedSongs
                    .filter {
                        !it.artist.isNullOrBlank()
                    }
                    .groupBy {
                        it.artist!!
                    }
                    .filterKeys {
                        !existingArtists.contains(
                            it
                        )
                    }
                    .map { (artistName, group) ->
                        val artist = Artist()
                        artist.name = artistName
                        artist.number_of_tracks =
                            group.size
                        artist
                    }
            if (artists.isNotEmpty()) {
                artistDao.insert(
                    artists
                )
            }

            // ----------------------------------------------------
            // Album
            // ----------------------------------------------------

            val existingAlbums =
                albumDao
                    .selectAll()
                    .map {
                        it.album_id
                    }
                    .toHashSet()

            val albums =
                addedSongs
                    .filter {
                        it.albumId > 0
                    }
                    .groupBy {
                        it.albumId
                    }
                    .filterKeys {
                        !existingAlbums.contains(
                            it
                        )
                    }
                    .map { (albumId, group) ->
                        val album = Album()
                        album.album_id =
                            albumId
                        album.album_name =
                            albumNameMap[
                                albumId
                            ] ?: "未知专辑"
                        album.number_of_songs =
                            group.size
                        album
                    }
            if (albums.isNotEmpty()) {
                albumDao.insert(
                    albums
                )
            }

            // ----------------------------------------------------
            // Folder
            // ----------------------------------------------------

            val existingFolders =
                folderDao
                    .selectAll()
                    .mapNotNull {
                        it.path
                    }
                    .toHashSet()
            val folders =
                addedSongs
                    .filter {
                        !it.folder.isNullOrBlank()
                    }
                    .groupBy {
                        it.folder!!
                    }
                    .filterKeys {
                        !existingFolders.contains(
                            it
                        )
                    }
                    .map { (path, group) ->
                        val folder =
                            Folder()
                        folder.path =
                            path
                        folder.name =
                            path.substringAfterLast(
                                File.separator
                            )
                        folder
                    }
            if (folders.isNotEmpty()) {
                folderDao.insert(
                    folders
                )
            }
        }
        return addedSongs.size
    }

    // ============================================================
    // 查询 Album Name
    // ============================================================

    /**
     * 根据歌曲的 albumId 查询 MediaStore 中对应的专辑名称。
     *
     * 返回：
     *
     * albumId -> albumName
     */
    @SuppressLint("Range")
    fun queryAlbumNameMap(
        context: Context,
        songs: List<Music>
    ): Map<Int, String> {
        val albumIds =
            songs
                .map {
                    it.albumId
                }
                .filter {
                    it > 0
                }
                .distinct()
        if (albumIds.isEmpty()) {
            return emptyMap()
        }
        val result =
            HashMap<Int, String>()
        val uri =
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection =
            arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM
            )
        /**
         * 创建：
         *
         * _id IN (?, ?, ?)
         */
        val placeholders =
            albumIds.joinToString(",") {
                "?"
            }
        val selection =
            "${MediaStore.Audio.Albums._ID} " +
                    "IN ($placeholders)"
        val selectionArgs =
            albumIds
                .map {
                    it.toString()
                }
                .toTypedArray()
        val cursor =
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )
        cursor?.use {
            val idIndex =
                it.getColumnIndex(
                    MediaStore.Audio.Albums._ID
                )
            val nameIndex =
                it.getColumnIndex(
                    MediaStore.Audio.Albums.ALBUM
                )
            while (it.moveToNext()) {
                val albumId =
                    it.getInt(
                        idIndex
                    )
                val albumName =
                    it.getString(
                        nameIndex
                    )
                if (!albumName.isNullOrBlank()) {
                    result[
                        albumId
                    ] = albumName
                }
            }
        }
        return result
    }
}