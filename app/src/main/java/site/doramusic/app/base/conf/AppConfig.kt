package site.doramusic.app.base.conf

import dora.util.IoUtils

interface AppConfig {

    companion object {

        const val APP_NAME = "Dora Music"
        const val APP_PACKAGE_NAME = "site.doramusic.app"
        const val ALBUM_TEXT = "仅用于学习交流，禁止用于包括但不仅限于商业用途，本产品由https://dorachat.com赞助"
        const val APP_SLOGAN = "版权所有，侵权必究"
        const val COPY_RIGHT = "doramusic ©2023"

        // 域名
        const val URL_APP_SERVER = "http://doramusic.site:8080"
        const val URL_AD_SERVER = "http://dorachat.com:9091"

        // Intent & Action
        const val MEDIA_SERVICE = "site.doramusic.app.service.MEDIA_SERVICE"
        const val ACTION_PREV = "site.doramusic.app.intent.ACTION_PREV"
        const val ACTION_NEXT = "site.doramusic.app.intent.ACTION_NEXT"
        const val ACTION_PAUSE_RESUME = "site.doramusic.app.intent.ACTION_PAUSE_RESUME"
        const val EXTRA_IS_PLAYING = "isPlaying"
        const val EXTRA_TITLE = "title"
        const val EXTRA_URL = "url"

        // 文件夹相关
        val FOLDER_LOG = IoUtils.getSdRoot() + "/DoraMusic/log" // 日志存放目录
        val FOLDER_LRC = IoUtils.getSdRoot() + "/DoraMusic/lrc" // 歌词文件存放目录

        const val MAX_RECENT_MUSIC_NUM = 100
        const val MUSIC_MENU_GRID_COLUMN_NUM = 3

        // 数据库相关
        const val DB_NAME = "db_doramusic"
        const val DB_VERSION = 1

        // 页面路由
        const val ROUTE_START_FROM_LOCAL = 1
        const val ROUTE_START_FROM_ARTIST = 2
        const val ROUTE_START_FROM_ALBUM = 3
        const val ROUTE_START_FROM_FOLDER = 4
        const val ROUTE_START_FROM_FAVORITE = 5
        const val ROUTE_START_FROM_LATEST = 6
        const val ROUTE_ARTIST_TO_LOCAL = 7
        const val ROUTE_ALBUM_TO_LOCAL = 8
        const val ROUTE_FOLDER_TO_LOCAL = 9

        // 播放状态
        const val MPS_NO_FILE = -1 // 无音乐文件
        const val MPS_INVALID = 0 // 当前音乐文件无效
        const val MPS_PREPARE = 1 // 准备就绪
        const val MPS_PLAYING = 2 // 播放中
        const val MPS_PAUSE = 3 // 暂停

        // 播放模式
        const val MPM_PLAYLIST_LOOP = 0 // 列表循环
        const val MPM_SEQUENTIAL_PLAYBACK = 1 // 顺序播放
        const val MPM_SHUFFLE_PLAYBACK = 2 // 随机播放
        const val MPM_SINGLE_TRACK_LOOP = 3 // 单曲循环

        // 扫描器过滤器
        const val SCANNER_FILTER_SIZE = 1024 * 1024 // 1MB
        const val SCANNER_FILTER_DURATION = 60 * 1000 // 1分钟

        const val MUSIC_LIST_MAX_LIST = 1000

        const val COLOR_THEME = "skin_theme_color"
    }
}
