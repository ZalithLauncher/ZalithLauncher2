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
import org.minidns.hla.DnssecResolverApi
import org.minidns.record.SRV
import java.util.Random

private const val TAG = "ServerRedirect"

/**
 * 在默认端口尝试 SRV 重定向
 */
/**
 * 在默认端口尝试 SRV 重定向
 */
suspend fun lookupRedirect(original: ServerAddress): ServerAddress? {
    if (original.port != ServerAddress.DEFAULT_PORT) return null

    return try {
        val tcpLink = "_minecraft._tcp.${original.getASCIIHost("")}"

        val result = withContext(Dispatchers.IO) {
            DnssecResolverApi.INSTANCE.resolveSrv(tcpLink)
        }

        val srvRecords = result.answers
        if (srvRecords.isEmpty()) return null

        val selected = selectSrvRecord(srvRecords)

        ServerAddress(
            selected.target.toString(),
            selected.port
        )
    } catch (e: Exception) {
        Logger.warning(TAG, "Unable to redirect server $original", e)
        null
    }
}

private fun selectSrvRecord(records: Set<SRV>): SRV {
    val minPriority = records.minOf { it.priority }
    val samePriority = records.filter { it.priority == minPriority }

    val totalWeight = samePriority.sumOf { it.weight }
    if (totalWeight <= 0) return samePriority.random()

    val rand = Random().nextInt(totalWeight)
    var acc = 0

    for (record in samePriority) {
        acc += record.weight
        if (rand < acc) {
            return record
        }
    }

    return samePriority.first()
}