/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.version.multiplayer

import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.ServerAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException

private const val TAG = "ServerAddressResolver"

data class ResolvedServerAddress(
    val hostName: String,
    val hostIp: String?,
    val port: Int,
    val address: InetSocketAddress
)

private fun InetSocketAddress.toData(): ResolvedServerAddress {
    return ResolvedServerAddress(
        hostName = address.hostName,
        hostIp = address.hostAddress,
        port = port,
        address = this
    )
}

/**
 * 尝试解析服务器地址
 * @return 解析后的服务器地址，如果为 null 则表示解析失败
 */
suspend fun ServerAddress.resolve(): ResolvedServerAddress {
    return withContext(Dispatchers.IO) {
        fun ServerAddress.resolveInternal(): ResolvedServerAddress? {
            return try {
                val resolvedAddress = InetAddress.getByName(getASCIIHost(""))
                val address = InetSocketAddress(resolvedAddress, port)
                address.toData()
            } catch (e: UnknownHostException) {
                Logger.warning(TAG, "Couldn't resolve server $host address", e)
                null
            }
        }

        val resolved = resolveInternal()

        val redirected = lookupRedirect(this@resolve)
        if (redirected != null) {
            val redirectedResolved = redirected.resolveInternal()
            if (redirectedResolved != null) {
                return@withContext redirectedResolved
            }
        }

        if (resolved == null) {
            throw IOException("Couldn't resolve and redirect server $host address.")
        }

        resolved
    }
}