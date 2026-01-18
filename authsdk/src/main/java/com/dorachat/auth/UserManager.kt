package com.dorachat.auth

class UserManager {
    
    var currentUser: DoraUser? = null
        private set
    var userInfo: DoraUserInfo? = null
        private set
    var userAvatar: String? = null
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
        userAvatar = null
    }

    fun setCurrentUser(user: DoraUser) {
        currentUser = user
        if (!userMap.containsKey(user.erc20)) {
            userMap[user.erc20] = user
        }
    }

    fun setUserInfo(userInfo: DoraUserInfo?) {
        this.userInfo = userInfo
    }

    fun setUserAvatar(userAvatar: String?) {
        this.userAvatar = userAvatar
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