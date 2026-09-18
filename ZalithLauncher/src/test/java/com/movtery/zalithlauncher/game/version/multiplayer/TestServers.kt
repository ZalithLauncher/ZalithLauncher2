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

import com.movtery.zalithlauncher.utils.network.ServerAddress
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestServers {

    @Test
    fun testSingleServerData() {
        val ip = "cn.mccisland.net"
        val address = ServerAddress.parse(ip)

        runBlocking {
            launch {
                runCatching {
                    val resolvedAddress = address.resolve()
                    val result = pingServer(resolvedAddress)
                    println("ip = $ip, Ping = ${result.pingMs}, status = ${result.status}")
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }
}