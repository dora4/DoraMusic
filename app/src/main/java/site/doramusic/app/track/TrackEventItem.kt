package site.doramusic.app.track

data class TrackEventItem(
    val eventName: String? = null,
    val eventTime: Long? = null,
    val extraData: Map<String, Any>? = null
)