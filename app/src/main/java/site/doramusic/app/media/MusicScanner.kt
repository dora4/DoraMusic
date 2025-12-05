package site.doramusic.app.media

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dora.db.Orm
import dora.db.Transaction
import dora.db.dao.DaoFactory
import dora.db.table.TableManager
import dora.util.PinyinUtils
import dora.util.TextUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import site.doramusic.app.base.conf.AppConfig
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
 */
@SuppressLint("Range")
object MusicScanner : AppConfig {

    private val projMusic = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION)
    private val projAlbum = arrayOf(MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART)
    private val projArtist = arrayOf(
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
    private val projFolder = arrayOf(MediaStore.Files.FileColumns.DATA)

    private val musicDao = DaoFactory.getDao(Music::class.java)
    private val artistDao = DaoFactory.getDao(Artist::class.java)
    private val albumDao = DaoFactory.getDao(Album::class.java)
    private val folderDao = DaoFactory.getDao(Folder::class.java)

    private const val DEFENSE_SQL_INJECTION_HEADER = " 1=1 "

    private fun recreateTables() {
        Transaction.execute {
            TableManager.recreateTable(Music::class.java)
            TableManager.recreateTable(Artist::class.java)
            TableManager.recreateTable(Album::class.java)
            TableManager.recreateTable(Folder::class.java)
        }
    }

    @JvmStatic
    fun scan(context: Context): Observable<List<Music>> {
        return Observable.fromCallable {
            try {
                recreateTables()
            } catch (ignored: Exception) {
            }
            var musics: List<Music> = arrayListOf()
            Transaction.execute(Music::class.java) {
                musics = queryMusic(context, AppConfig.ROUTE_START_FROM_LOCAL)
                it.insert(musics)
            }
            if (musics.isNotEmpty()) {
                Transaction.execute {
                    val artists = queryArtist(context)
                    artistDao.insert(artists)

                    val albums = queryAlbum(context)
                    albumDao.insert(albums)

                    val folders = queryFolder(context)
                    folderDao.insert(folders)
                }
            }
            musics
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    @JvmStatic
    fun queryMusic(context: Context, from: Int): List<Music> {
        return queryMusic(context, null, null, from)
    }

    @JvmStatic
    fun queryMusic(context: Context,
                   selections: String?, selection: String?, from: Int): List<Music> {
        val sp = PrefsManager(context)
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cr = context.contentResolver
        val select = StringBuffer(DEFENSE_SQL_INJECTION_HEADER)
        // 查询语句：检索出时长大于1分钟，文件大小大于1MB的媒体文件
        if (sp.getFilterSize()) {
            select.append(" AND ${MediaStore.Audio.Media.SIZE} > " +
                    "${AppConfig.SCANNER_FILTER_SIZE}")
        }
        if (sp.getFilterTime()) {
            select.append(" AND ${MediaStore.Audio.Media.DURATION} > " +
                    "${AppConfig.SCANNER_FILTER_DURATION}")
        }
        if (TextUtils.isNotEmpty(selections)) {
            select.append(selections)
        }
        return when (from) {
            AppConfig.ROUTE_START_FROM_LOCAL -> if (musicDao.count() > 0) {
                musicDao.selectAll()
            } else {
                getMusicList(cr.query(uri, projMusic,
                        select.toString(), null,
                        MediaStore.Audio.Media.ARTIST_KEY))
            }
            AppConfig.ROUTE_START_FROM_ARTIST -> if (musicDao.count() > 0) {
                queryMusic(selection,
                        AppConfig.ROUTE_START_FROM_ARTIST)
            } else {
                getMusicList(cr.query(uri, projMusic,
                        select.toString(), null,
                        MediaStore.Audio.Media.ARTIST_KEY))
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                if (musicDao.count() > 0) {
                    return queryMusic(selection, AppConfig.ROUTE_START_FROM_ALBUM)
                }
                if (musicDao.count() > 0) {
                    return queryMusic(selection, AppConfig.ROUTE_START_FROM_FOLDER)
                }
                if (musicDao.count() > 0) {
                    return queryMusic(selection, AppConfig.ROUTE_START_FROM_FAVORITE)
                }
                if (musicDao.count() > 0) {
                    queryMusic(selection, AppConfig.ROUTE_START_FROM_LATEST)
                } else arrayListOf()
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                if (musicDao.count() > 0) {
                    return queryMusic(selection, AppConfig.ROUTE_START_FROM_FOLDER)
                }
                if (musicDao.count() > 0) {
                    return queryMusic(selection, AppConfig.ROUTE_START_FROM_FAVORITE)
                }
                if (musicDao.count() > 0) {
                    queryMusic(selection, AppConfig.ROUTE_START_FROM_LATEST)
                } else arrayListOf()
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                if (musicDao.count() > 0) {
                    return queryMusic(selection, AppConfig.ROUTE_START_FROM_FAVORITE)
                }
                if (musicDao.count() > 0) {
                    queryMusic(selection, AppConfig.ROUTE_START_FROM_LATEST)
                } else arrayListOf()
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                if (musicDao.count() > 0) {
                    queryMusic(selection, AppConfig.ROUTE_START_FROM_LATEST)
                } else arrayListOf()
            }
            else -> arrayListOf()
        }
    }

    @JvmStatic
    fun queryMusic(selection: String?, type: Int): List<Music> {
        // 此处仅为演示原始SQL写法，建议使用WhereBuilder
        val sql = when (type) {
            AppConfig.ROUTE_START_FROM_ARTIST -> {
                "SELECT * FROM music WHERE ${Music.COLUMN_ARTIST} = ?"
            }
            AppConfig.ROUTE_START_FROM_ALBUM -> {
                "SELECT * FROM music WHERE ${Music.COLUMN_ALBUM_ID} = ?"
            }
            AppConfig.ROUTE_START_FROM_FOLDER -> {
                "SELECT * FROM music WHERE ${Music.COLUMN_FOLDER} = ?"
            }
            AppConfig.ROUTE_START_FROM_FAVORITE -> {
                "SELECT * FROM music WHERE ${Music.COLUMN_FAVORITE} = ?"
            }
            AppConfig.ROUTE_START_FROM_LATEST -> {
                "SELECT * FROM music WHERE ${Music.COLUMN_LAST_PLAY_TIME} > ? ORDER BY " +
                        "${Music.COLUMN_LAST_PLAY_TIME} DESC LIMIT 100"
            }
            // 没有这种情况
            else -> { "SELECT * FROM music" }
        }
        return parseCursor(Orm.getDB().rawQuery(sql, arrayOf(selection)))
    }

    private fun parseCursor(cursor: Cursor): List<Music> {
        val list: MutableList<Music> = ArrayList()
        while (cursor.moveToNext()) {
            val music = Music()
            music.id = cursor.getInt(cursor.getColumnIndex(Music.COLUMN_ID))
            music.songId = cursor.getInt(cursor.getColumnIndex(Music.COLUMN_SONG_ID))
            music.albumId = cursor.getInt(cursor.getColumnIndex(Music.COLUMN_ALBUM_ID))
            music.duration = cursor.getInt(cursor.getColumnIndex(Music.COLUMN_DURATION))
            music.musicName = cursor.getString(cursor.getColumnIndex(
                Music.COLUMN_MUSIC_NAME))
            music.artist = cursor.getString(cursor.getColumnIndex(Music.COLUMN_ARTIST))
            music.data = cursor.getString(cursor.getColumnIndex(Music.COLUMN_DATA))
            music.folder = cursor.getString(cursor.getColumnIndex(Music.COLUMN_FOLDER))
            music.musicNameKey = cursor.getString(cursor.getColumnIndex(
                Music.COLUMN_MUSIC_NAME_KEY))
            music.artistKey = cursor.getString(cursor.getColumnIndex(
                Music.COLUMN_ARTIST_KEY))
            music.favorite = cursor.getInt(cursor.getColumnIndex(Music.COLUMN_FAVORITE))
            music.lastPlayTime = cursor.getLong(cursor.getColumnIndex(
                Music.COLUMN_LAST_PLAY_TIME))
            list.add(music)
        }
        cursor.close()
        return list
    }

    /**
     * 获取包含音频文件的文件夹信息。
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun queryFolder(context: Context): List<Folder> {
        val sp = PrefsManager(context)
        val uri = MediaStore.Files.getContentUri("external")
        val cr = context.contentResolver

        val audioExtensions = listOf("mp3", "flac", "wav", "ape", "m4a", "aac")
        val likeConditions = audioExtensions.joinToString(" OR ") { ext ->
            "${MediaStore.Files.FileColumns.DATA} LIKE '%.${ext}'"
        }
        val selection = StringBuilder().apply {
            append("${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}")
            append(" AND (")
            append(likeConditions)
            append(")")
        }
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
        if (sp.getFilterSize()) {
            selection.append(" AND " + MediaStore.Audio.Media.SIZE + " > " + AppConfig.SCANNER_FILTER_SIZE)
        }
        if (sp.getFilterTime()) {
            selection.append(" AND " + MediaStore.Audio.Media.DURATION + " > " + AppConfig.SCANNER_FILTER_DURATION)
        }
//        selection.append(") group by ( " + MediaStore.Files.FileColumns.PARENT)
        return if (folderDao.count() > 0) {
            folderDao.selectAll()
        } else {
            getFolderList(cr.query(uri, projFolder, selection.toString(), null, null))
        }
    }

    /**
     * 获取歌手信息。
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun queryArtist(context: Context): List<Artist> {
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val cr = context.contentResolver
        return if (artistDao.count() > 0) {
            artistDao.selectAll()
        } else {
            getArtistList(cr.query(uri, projArtist,
                    null, null, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                    + " DESC"))
        }
    }

    /**
     * 获取专辑信息。
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun queryAlbum(context: Context): List<Album> {
        val sp = PrefsManager(context)
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val cr = context.contentResolver
        val where = StringBuilder(MediaStore.Audio.Albums._ID
                + " IN (SELECT DISTINCT " + MediaStore.Audio.Media.ALBUM_ID
                + " FROM audio_meta WHERE (1=1 ")
        if (sp.getFilterSize()) {
            where.append(" AND " + MediaStore.Audio.Media.SIZE + " > " + AppConfig.SCANNER_FILTER_SIZE)
        }
        if (sp.getFilterTime()) {
            where.append(" AND " + MediaStore.Audio.Media.DURATION + " > " + AppConfig.SCANNER_FILTER_DURATION)
        }
        where.append("))")
        return if (albumDao.count() > 0) {
            albumDao.selectAll()
        } else { // Media.ALBUM_KEY 按专辑名称排序
            // FIXME:  Android11的Invalid token select问题
            getAlbumList(cr.query(uri, projAlbum,
                    null, null, MediaStore.Audio.Media.ALBUM_KEY))
        }
    }

    private fun getMusicList(cursor: Cursor?): List<Music> {
        val list: MutableList<Music> = ArrayList()
        if (cursor == null) {
            return list
        }
        while (cursor.moveToNext()) {
            val music = Music()
            val filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA))
            music.songId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID))
            music.albumId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            val duration = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION))
            if (duration > 0) {
                music.duration = duration
            } else {
                try {
                    music.duration = MusicUtils.getDuration(filePath)
                } catch (e: RuntimeException) {
                    continue
                }
            }
            music.musicName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE))
            music.artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST))
            music.data = filePath
            val folderPath = filePath.take(filePath.lastIndexOf(File.separator))
            music.folder = folderPath
            music.musicNameKey = PinyinUtils.getPinyinFromSentence(music.musicName)
            music.artistKey = PinyinUtils.getPinyinFromSentence(music.artist)
            list.add(music)
        }
        cursor.close()
        return list
    }

    private fun getAlbumList(cursor: Cursor?): List<Album> {
        val list: MutableList<Album> = ArrayList()
        if (cursor == null) {
            return list
        }
        while (cursor.moveToNext()) {
            val album = Album()
            album.album_name = cursor.getString(
                cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
            album.album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID))
            album.number_of_songs = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
            album.album_cover_path = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            list.add(album)
        }
        cursor.close()
        return list
    }

    private fun getArtistList(cursor: Cursor?): List<Artist> {
        val list: MutableList<Artist> = ArrayList()
        if (cursor == null) {
            return list
        }
        while (cursor.moveToNext()) {
            val artist = Artist()
            artist.name = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.ARTIST))
            artist.number_of_tracks = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
            list.add(artist)
        }
        cursor.close()
        return list
    }

    private fun getFolderList(cursor: Cursor?): List<Folder> {
        val list: MutableList<Folder> = ArrayList()
        if (cursor == null) {
            return list
        }
        while (cursor.moveToNext()) {
            val folder = Folder()
            val filePath = cursor.getString(
                cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
            folder.path = filePath.take(filePath.lastIndexOf(File.separator))
            folder.name = folder.path.substring(folder.path
                    .lastIndexOf(File.separator) + 1)
            list.add(folder)
        }
        cursor.close()
        return list
    }
}