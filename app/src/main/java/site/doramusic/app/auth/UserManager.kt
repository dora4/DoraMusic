package site.doramusic.app.auth

class UserManager {
    
    var currentUser: DoraUser? = null
        private set
    var userInfo: DoraUserInfo? = null
        private set

    private val userMap: MutableMap<String, DoraUser> = HashMap()

    val userList: List<DoraUser>
        get() {
            val models: MutableList<DoraUser> = ArrayList()
            userMap.entries.forEach {
                models.add(it.value)
            }
            return models
        }

    fun removeCurrentUser() {
        if (userMap.containsKey(currentUser?.erc20)) {
            userMap.remove(currentUser?.erc20)
        }
        currentUser = null
        userInfo = null
    }

    fun setCurrentUser(chatUser: DoraUser) {
        currentUser = chatUser
        if (!userMap.containsKey(chatUser.erc20)) {
            userMap[chatUser.erc20] = chatUser
        }
    }

    fun setUserInfo(userInfo: DoraUserInfo) {
        this.userInfo = userInfo
    }

    companion object {

        private var instance: UserManager? = null
        @JvmStatic
        val ins: UserManager?
            get() {
                if (instance == null) {
                    synchronized(UserManager::class.java) {
                        if (instance == null) instance = UserManager()
                    }
                }
                return instance
            }
    }
}