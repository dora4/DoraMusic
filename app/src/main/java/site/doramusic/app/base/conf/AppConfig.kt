package site.doramusic.app.base.conf

import dora.util.IoUtils
import java.util.Calendar

interface AppConfig {

    companion object {

        // 产品和版权
        const val APP_NAME = "Dora Music"
        const val PRODUCT_NAME = "doramusic"
        const val APP_PACKAGE_NAME = "site.doramusic.app"
        const val ALBUM_TEXT = "仅用于学习交流，禁止用于包括但不仅限于商业用途，本产品由https://dorachat.com赞助"
        const val APP_SLOGAN = "版权所有，侵权必究"
        val COPY_RIGHT = "doramusic ©2023~${Calendar.getInstance().get(Calendar.YEAR)}"

        // 域名
        const val URL_APP_SERVER = "http://doramusic.site:8080"
        const val URL_AD_SERVER = "http://dorachat.com:9091"

        // key
        const val DORA_FUND_ACCESS_KEY = "vs42INhGWDnq"
        const val DORA_FUND_SECRET_KEY = "RrZqzf1Vh8StMqyHhpfCu6TPOQMoCRYw"
        const val PGYER_API_KEY = "b32485d39298de8a302c67883e192107"
        const val PGYER_APP_KEY = "ee2ab0aa8ba49f78e2ac1cf4f1d54c66"
        const val DISCORD_GROUP_INVITE_CODE = "HUx8dDSZaP"


        // Intent & Action
        const val MEDIA_SERVICE = "site.doramusic.app.service.MEDIA_SERVICE"
        const val ACTION_PREV = "site.doramusic.app.intent.ACTION_PREV"
        const val ACTION_NEXT = "site.doramusic.app.intent.ACTION_NEXT"
        const val ACTION_PAUSE_RESUME = "site.doramusic.app.intent.ACTION_PAUSE_RESUME"
        const val EXTRA_IS_PLAYING = "isPlaying"
        const val EXTRA_TITLE = "title"
        const val EXTRA_URL = "url"

        // 文件夹相关
        const val LOG_PATH = "DoraMusic/log"
        val FOLDER_LOG = IoUtils.getSdRoot() + "/$LOG_PATH" // 日志存放目录
        val FOLDER_LRC = IoUtils.getSdRoot() + "/DoraMusic/lrc" // 歌词文件存放目录

        // 数据库相关
        const val DB_NAME = "db_doramusic"
        const val DB_VERSION = 4
        const val COLUMN_ORDER_ID = "order_id"
        const val COLUMN_TOKEN_AMOUNT = "token_amount"
        const val COLUMN_TOKEN_SYMBOL = "token_symbol"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_PENDING = "pending"
        const val COLUMN_TRANSACTION_HASH = "transaction_hash"

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
        const val MPM_PLAYLIST_LOOP = 0x1 // 列表循环
        const val MPM_SEQUENTIAL_PLAYBACK = 0x2 // 顺序播放
        const val MPM_SHUFFLE_PLAYBACK = 0x3 // 随机播放
        const val MPM_SINGLE_TRACK_LOOP = 0x4 // 单曲循环

        // 扫描器过滤器
        const val SCANNER_FILTER_SIZE = 1024 * 1024 // 1MB
        const val SCANNER_FILTER_DURATION = 60 * 1000 // 1分钟

        // 基本配置
        const val MUSIC_LIST_MAX_LIST = 1000 // 播放列表的最大限制
        const val MAX_RECENT_MUSIC_NUM = 100    // 最近播放的最大限制
        const val MUSIC_MENU_GRID_COLUMN_NUM = 3    // 功能网格每行显示的个数

        // 换肤
        const val COLOR_THEME = "skin_theme_color" // 主色调的换肤key
    }
}
