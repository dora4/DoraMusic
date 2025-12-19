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

        // Http设置
        const val CONNECT_TIMEOUT = 3L
        const val READ_TIMEOUT = 3L

        // 域名，仅展示在钱包，不校验，随便写
        const val URL_DOMAIN = "http://doramusic.site"
        const val URL_COMMON_API = "http://dorachat.com:9696/api"
        const val URL_FILE_SERVER = "http://dorachat.com:9999"
        const val URL_AD_SERVER = URL_COMMON_API

        // api key
        const val DORA_FUND_ACCESS_KEY = "vs42INhGWDnq"
        // api secret，不要泄露给任何人，开源项目无所谓
        const val DORA_FUND_SECRET_KEY = "RrZqzf1Vh8StMqyHhpfCu6TPOQMoCRYw"
        const val PGYER_API_KEY = "b32485d39298de8a302c67883e192107"
        const val PGYER_APP_KEY = "ee2ab0aa8ba49f78e2ac1cf4f1d54c66"
        const val DISCORD_GROUP_INVITE_CODE = "HUx8dDSZaP"


        // Intent & Action
        const val MEDIA_SERVICE = "site.doramusic.app.service.MEDIA_SERVICE"
        const val ACTION_PREV = "site.doramusic.app.intent.ACTION_PREV"
        const val ACTION_NEXT = "site.doramusic.app.intent.ACTION_NEXT"
        const val ACTION_FAVORITE = "site.doramusic.app.intent.ACTION_FAVORITE"
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

        const val CONF_ENABLE_BANNER_AD = "enable_banner_ad"

        // 100首推荐歌曲
        val SONG_MAP = mapOf(
            "是你" to "https://www.youtube.com/watch?v=aM0EBp9OaAM",
            "爱错" to "https://www.youtube.com/watch?v=AQLuz0wamT8",
            "谁" to "https://www.youtube.com/watch?v=8z-C8dikNjA",
            "离别开出花" to "https://www.youtube.com/watch?v=ZYt5Cg4Qqbs",
            "阿衣莫" to "https://www.youtube.com/watch?v=O1kXtPGjOzw",
            "精卫" to "https://www.youtube.com/watch?v=YtFQZkGZtLc",
            "不谓侠" to "https://www.youtube.com/watch?v=DgC942kpOsM",
            "春庭雪" to "https://www.youtube.com/watch?v=newAggUqhts",
            "卜卦" to "https://www.youtube.com/watch?v=EWGdVNUVYbE",
            "海市蜃楼" to "https://www.youtube.com/watch?v=yB8HmL3WSK8",
            "探故知" to "https://www.youtube.com/watch?v=5ELID57kRPg",
            "难却" to "https://www.youtube.com/watch?v=KonvHhu3LZU",
            "莫问归期" to "https://www.youtube.com/watch?v=j1WifUe_fjQ",
            "无情画" to "https://www.youtube.com/watch?v=LXFKhiAkmso",
            "辞九门回忆" to "https://www.youtube.com/watch?v=bQ-SVxu-_DI",
            "飞鸟和蝉" to "https://www.youtube.com/watch?v=-VjwtAYHzBk",
            "如愿" to "https://www.youtube.com/watch?v=IOb_IX3u2ag",
            "过火" to "https://www.youtube.com/watch?v=Hj8P88ZtrwM",
            "敢爱敢做" to "https://www.youtube.com/watch?v=HGmTVMZR0hE",
            "春不晚" to "https://www.youtube.com/watch?v=uYGN77Cww-w",
            "半点心" to "https://www.youtube.com/watch?v=sIucMXINXaI",
            "大天蓬" to "https://www.youtube.com/watch?v=7-_4NcjDlBs",
            "你的万水千山" to "https://www.youtube.com/watch?v=BZQkb7KpOf0",
            "相思遥" to "https://www.youtube.com/watch?v=ToyWa0Of1ns",
            "典狱司" to "https://www.youtube.com/watch?v=3H7YHwep2hk",
            "鸳鸯戏" to "https://www.youtube.com/watch?v=D038bYY7h-U",
            "野孩子" to "https://www.youtube.com/watch?v=KYZW55KJrK0",
            "赤伶" to "https://www.youtube.com/watch?v=HOBOBgmzuGo",
            "只为你着迷" to "https://music.youtube.com/watch?v=chk9qD70MtU",
            "火红的萨日朗" to "https://www.youtube.com/watch?v=qiYwASqE960",
            "女孩" to "https://www.youtube.com/watch?v=NszjMg8vVhA",
            "从前说" to "https://www.youtube.com/watch?v=qjAKle8bz2E",
            "风催雨" to "https://www.youtube.com/watch?v=h0tojPD-65g",
            "美人画卷" to "https://www.youtube.com/watch?v=Ai8dlJlWeuE",
            "选择失忆" to "https://www.youtube.com/watch?v=Sfd8qBKvdMg",
            "秒针" to "https://www.youtube.com/watch?v=tN6-VYvstGM",
            "爱情有时很残忍" to "https://www.youtube.com/watch?v=H6TUN01DoQQ",
            "孤城" to "https://www.youtube.com/watch?v=ChRYTbz67IE",
            "虞兮叹" to "https://www.youtube.com/watch?v=ACmrAE4ov94",
            "科目三" to "https://www.youtube.com/watch?v=b3rFbkFjRrA",
            "起风了" to "https://www.youtube.com/watch?v=s5nFn4RgelA",
            "游京" to "https://www.youtube.com/watch?v=OjQ0KqCJOjk",
            "Everytime We Touch" to "https://www.youtube.com/watch?v=TQ_oIxIDKTA",
            "须尽欢" to "https://www.youtube.com/watch?v=LXDi5qNu4xA",
            "夜色" to "https://www.youtube.com/watch?v=aatVmb9ZCws",
            "快乐阿拉蕾" to "https://www.youtube.com/watch?v=4haZeezqMio",
            "别让爱凋落" to "https://www.youtube.com/watch?v=93U7ifus358",
            "跳楼机" to "https://www.youtube.com/watch?v=Hkd45-teUzg",
            "列车开往春天" to "https://www.youtube.com/watch?v=ZQg0ezB-q8g",
            "北京欢迎你" to "https://www.youtube.com/watch?v=_Pm80NKWuks",
            "赐我" to "https://www.youtube.com/watch?v=UwQjBW5pm1A",
            "樱花树下的约定" to "https://www.youtube.com/watch?v=8plzbfMYkCU",
            "忘川彼岸" to "https://www.youtube.com/watch?v=dhF0ANt9erI",
            "想某人" to "https://www.youtube.com/watch?v=xWegMGxyetY",
            "折风渡夜" to "https://www.youtube.com/watch?v=vNRFV0SITFs",
            "姑娘在远方" to "https://www.youtube.com/watch?v=dSOz373wvrk",
            "罗曼蒂克的爱情" to "https://www.youtube.com/watch?v=0tPahfiueJ0",
            "陪我过个冬" to "https://www.youtube.com/watch?v=o7pyoXc1-CM",
            "末班车" to "https://www.youtube.com/watch?v=7j16GT6DTEM",
            "最近" to "https://www.youtube.com/watch?v=nn2Z7EgdRpA",
            "如果爱忘了" to "https://www.youtube.com/watch?v=cKN20gqTTwk",
            "忘了" to "https://www.youtube.com/watch?v=paNgLF8tLjs",
            "把回忆拼好给你" to "https://www.youtube.com/watch?v=rFWcBkBRTkU",
            "满天星辰不及你" to "https://www.youtube.com/watch?v=bmnRFkpIunI",
            "爱不得忘不舍" to "https://www.youtube.com/watch?v=h1pzMFgNpcQ",
            "嘉宾" to "https://www.youtube.com/watch?v=j1wDEcxC2wg",
            "三拜红尘凉" to "https://www.youtube.com/watch?v=Pzx1tctmFB4",
            "悬溺" to "https://www.youtube.com/watch?v=U9Z9X_YXaNY",
            "青衣" to "https://www.youtube.com/watch?v=HSoXfMPhz_w",
            "谪仙" to "https://www.youtube.com/watch?v=9wJ9uhxoEc4",
            "好心分手" to "https://www.youtube.com/watch?v=xsmdIPPOIZw",
            "我知道你不爱我" to "https://www.youtube.com/watch?v=JFRTSDkiSsk",
            "我又想你了" to "https://www.youtube.com/watch?v=jMX0hJCumbE",
            "It's My Life" to "https://www.youtube.com/watch?v=vx2u5uUu3DE",
            "三月里的小雨" to "https://www.youtube.com/watch?v=H-lOpI74KlU",
            "画离弦" to "https://www.youtube.com/watch?v=0VOmsnmEMOo",
            "流着泪说分手" to "https://www.youtube.com/watch?v=p6HLYyJjcqo",
            "纸短情长" to "https://www.youtube.com/watch?v=WQ2v_7IVCUA",
            "我曾用心爱着你" to "https://www.youtube.com/watch?v=ip9nkfDQSFU",
            "落差" to "https://www.youtube.com/watch?v=FkK7SLdZpbM",
            "偷心" to "https://www.youtube.com/watch?v=CWm9oJJ0V7g",
            "未必" to "https://www.youtube.com/watch?v=dIWNReIyUL4",
            "先说谎的人" to "https://www.youtube.com/watch?v=N1ap5mNMuLY",
            "西楼爱情故事" to "https://www.youtube.com/watch?v=nU2nOCa8qCY",
            "Let Me Know" to "https://www.youtube.com/watch?v=aJSvAK6OCgk",
            "爱的专属权" to "https://www.youtube.com/watch?v=G6TqYDXzKnc",
            "浪子闲话" to "https://www.youtube.com/watch?v=3Fq7XwrMv3o",
            "求佛" to "https://www.youtube.com/watch?v=PrE5YuraYNg",
            "笑纳" to "https://www.youtube.com/watch?v=UF8UHdjwmoA",
            "叹云兮" to "https://www.youtube.com/watch?v=ozfGodF0Wss",
            "最后一页" to "https://www.youtube.com/watch?v=9t-UhvTIlSQ",
            "若月亮没来" to "https://www.youtube.com/watch?v=eKcqZUc66ac",
            "欢喜就好" to "https://www.youtube.com/watch?v=fHIMK2XpyHU",
            "感谢你曾来过" to "https://www.youtube.com/watch?v=xrvBuhNz7OM",
            "月亮照山川" to "https://www.youtube.com/watch?v=2MnVDj1anfQ",
            "Zombie" to "https://www.youtube.com/watch?v=2Gw71CYEMHs",
            "囧架架" to "https://www.youtube.com/watch?v=-P0zrrwFGgQ",
            "像鱼" to "https://www.youtube.com/watch?v=RDzNIvxrLWk",
            "黄昏" to "https://www.youtube.com/watch?v=o7sk2Kt_2hw",
            "New Boy" to "https://www.youtube.com/watch?v=mwYCjjK8g0w",
            "追梦人" to "https://www.youtube.com/watch?v=kqJMPfoa2NU"
        )
    }
}
