package site.doramusic.app.base.conf

import dora.util.IoUtils

interface AppConfig {

    companion object {
        const val URL_APP_SERVER = "http://doramusic.site:8080"
        const val URL_AD_SERVER = "http://dorachat.com:9091"

        /**
         * Intent & Action。
         */
        const val MEDIA_SERVICE = "site.doramusic.app.service.MEDIA_SERVICE"
        const val ACTION_PLAY = "site.doramusic.app.intent.ACTION_PLAY"
        const val ACTION_PREV = "site.doramusic.app.intent.ACTION_PREV"
        const val ACTION_NEXT = "site.doramusic.app.intent.ACTION_NEXT"
        const val ACTION_PAUSE_RESUME = "site.doramusic.app.intent.ACTION_PAUSE_RESUME"
        const val ACTION_CANCEL = "site.doramusic.app.intent.ACTION_CANCEL"

        // 文件夹相关
        val FOLDER_LOG = IoUtils.getSdRoot() + "/DoraMusic/log" //日志存放目录
        val FOLDER_LRC = IoUtils.getSdRoot() + "/DoraMusic/lrc" //歌词文件存放目录

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
        const val MPM_LIST_LOOP_PLAY = 0 // 列表循环
        const val MPM_ORDER_PLAY = 1 // 顺序播放
        const val MPM_RANDOM_PLAY = 2 // 随机播放
        const val MPM_SINGLE_LOOP_PLAY = 3 // 单曲循环

        // 扫描器过滤器
        const val SCANNER_FILTER_SIZE = 1024 * 1024 // 1MB
        const val SCANNER_FILTER_DURATION = 60 * 1000 // 1分钟

        const val MUSIC_LIST_MAX_LIST = 1000
    }
}
