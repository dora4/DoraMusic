package site.doramusic.app.base.conf;

import dora.util.IoUtils;

public interface AppConfig {

    /**
     * 朵拉音乐应用服务器。
     */
    String URL_APP_SERVER = "http://doramusic.site:8080";

    /**
     * 朵拉音乐文件服务器。
     */
    String URL_FILE_SERVER = "http://47.111.72.9:8080";

    /**
     * 第三方appkey。
     */
    String BUGLY_APP_ID = "2efb8a5451";
    String BMOB_APP_ID = "3511cb23ace2d895cafcc6c034d50033";
    String UMENG_APP_KEY = "5e753ea20cafb2836d0007f0";

    /**
     * Intent & Action。
     */
    String MEDIA_SERVICE = "site.doramusic.app.service.MEDIA_SERVICE";
    String ACTION_PLAY = "site.doramusic.app.intent.ACTION_PLAY";
    String ACTION_PREV = "site.doramusic.app.intent.ACTION_PREV";
    String ACTION_NEXT = "site.doramusic.app.intent.ACTION_NEXT";
    String ACTION_PAUSE_RESUME = "site.doramusic.app.intent.ACTION_PAUSE_RESUME";
    String ACTION_CANCEL = "site.doramusic.app.intent.ACTION_CANCEL";

    //文件夹
    String FOLDER_LRC = IoUtils.getSdRoot() + "/DoraMusic/lrc"; //歌词存放目录
    String FOLDER_SONG = IoUtils.getSdRoot() + "/DoraMusic/song";   //歌曲存放目录
    String FOLDER_APK = IoUtils.getSdRoot() + "/DoraMusic/apk"; //升级包存放目录
    String FOLDER_CACHE = IoUtils.getSdRoot() + "/DoraMusic/cache"; //缓存存放目录
    String FOLDER_COVER = IoUtils.getSdRoot() + "/DoraMusic/cover"; //专辑封面存放目录
    String FOLDER_LOG = IoUtils.getSdRoot() + "/DoraMusic/log"; //日志存放目录
    String FOLDER_PATCH = IoUtils.getSdRoot() +"/DoraMusic/patch";  //热修复补丁临时存放目录

    //页面路由
    int ROUTE_START_FROM_LOCAL = 1;
    int ROUTE_START_FROM_ARTIST = 2;
    int ROUTE_START_FROM_ALBUM = 3;
    int ROUTE_START_FROM_FOLDER = 4;
    int ROUTE_START_FROM_FAVORITE = 5;
    int ROUTE_START_FROM_LATEST = 6;
    int ROUTE_ARTIST_TO_LOCAL = 7;
    int ROUTE_ALBUM_TO_LOCAL = 8;
    int ROUTE_FOLDER_TO_LOCAL = 9;

    // 播放状态
    int MPS_NO_FILE = -1; // 无音乐文件
    int MPS_INVALID = 0; // 当前音乐文件无效
    int MPS_PREPARE = 1; // 准备就绪
    int MPS_PLAYING = 2; // 播放中
    int MPS_PAUSE = 3; // 暂停

    // 播放模式
    int MPM_LIST_LOOP_PLAY = 0; // 列表循环
    int MPM_ORDER_PLAY = 1; // 顺序播放
    int MPM_RANDOM_PLAY = 2; // 随机播放
    int MPM_SINGLE_LOOP_PLAY = 3; // 单曲循环

    // 扫描器过滤器
    int SCANNER_FILTER_SIZE = 1 * 1024 * 1024;  // 1MB
    int SCANNER_FILTER_DURATION = 1 * 60 * 1000;    // 1分钟
}
