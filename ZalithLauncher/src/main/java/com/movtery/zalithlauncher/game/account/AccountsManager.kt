/**
 * 
 */
private fun refreshCurrentAccountState() {
    val currentAccount = getCurrentAccount()
    val isOffline = checkLimit()
    _currentAccountFlow.update {
        //若处于非正版状态，不允许使用账号
        if (isOffline) null else currentAccount
    }
    _isOffline.update { isOffline }
}

private fun checkLimit(): Boolean {
    return false
}

/**
 * 保存账号到数据库
 */
fun saveAccount(account: Account) {
    scope.launch {
        suspendSaveAccount(account)
    }
}

/**
 * 保存账号到数据库
 */
suspend fun suspendSaveAccount(account: Account) {
    runCatching {
        accountDao.saveAccount(account)
        Logger.info(TAG, "Saved account: ${account.username}")
        //同时设置当前账号
        setCurrentAccountInternal(account)
    }.onFailure { e ->
        Logger.error(TAG, "Failed to save account: ${account.username}", e)
    }
    suspendReloadAccounts()
}

/**
 * 从数据库中删除账号，并刷新
 */
fun deleteAccount(account: Account) {
    scope.launch {
        accountDao.deleteAccount(account)
        val skinFile = account.getSkinFile()
        FileUtils.deleteQuietly(skinFile)
        suspendReloadAccounts()
    }
}

/**
 * 保存认证服务器到数据库
 */
suspend fun saveAuthServer(server: AuthServer) {
    runCatching {
        authServerDao.saveServer(server)
        Logger.info(TAG, "Saved auth server: ${server.serverName} -> ${server.baseUrl}")
    }.onFailure { e ->
        Logger.error(TAG, "Failed to save auth server: ${server.serverName}", e)
    }
    reloadAuthServers()
}

/**
 * 从数据库中删除认证服务器，并刷新
 */
fun deleteAuthServer(server: AuthServer) {
    scope.launch {
        authServerDao.deleteServer(server)
        reloadAuthServers()
    }
}

/**
 * 是否已登录过微软账号
 */
fun hasMicrosoftAccount(): Boolean = _accounts.any { it.isMicrosoftAccount() }

/**
 * 通过账号的profileId读取账号
 */
fun loadFromProfileID(
    profileId: String,
    accountType: String? = null
): Account? =
    _accounts.find { it.profileId == profileId && it.accountType == accountType }

/**
 * 账号是否存在
 */
fun isAccountExists(uniqueUUID: String): Boolean {
    return uniqueUUID.isNotEmpty() && _accounts.any { it.uniqueUUID == uniqueUUID }
}

/**
 * 认证服务器是否存在
 */
fun isAuthServerExists(baseUrl: String): Boolean {
    return baseUrl.isNotEmpty() && _authServers.any { it.baseUrl == baseUrl }
}
