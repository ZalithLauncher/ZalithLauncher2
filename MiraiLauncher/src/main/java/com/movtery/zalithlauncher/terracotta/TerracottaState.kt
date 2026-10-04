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

package com.movtery.zalithlauncher.terracotta

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.terracotta.TerracottaState.Exception.Type
import com.movtery.zalithlauncher.terracotta.profile.TerracottaProfile

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/bd6a6fa/HMCL/src/main/java/org/jackhuang/hmcl/terracotta/TerracottaState.java)
 */
@Keep
sealed class TerracottaState {
    open fun isForkOf(state: TerracottaState?): Boolean = false

    @Keep
    sealed class PortSpecific(
        @Transient
        var port: Int
    ) : TerracottaState()

    @Keep
    sealed class Ready(
        port: Int,
        @SerializedName("index")
        val index: Int,
        @SerializedName("state")
        val state: String
    ) : PortSpecific(port) {
        @StringRes
        abstract fun localStringRes(): Int
    }

    @Keep
    class Unknown(port: Int) : PortSpecific(port)

    @Keep
    class Waiting(port: Int, index: Int, state: String) : Ready(port, index, state) {
        override fun localStringRes(): Int = error("No op.")
    }

    @Keep
    class HostScanning(port: Int, index: Int, state: String) : Ready(port, index, state) {
        override fun localStringRes(): Int = R.string.terracotta_status_host_scanning
    }

    @Keep
    class HostStarting(port: Int, index: Int, state: String) : Ready(port, index, state) {
        override fun localStringRes(): Int = R.string.terracotta_status_host_starting
    }

    @Keep
    class HostOK(
        port: Int,
        index: Int,
        state: String,
        @SerializedName("room")
        val code: String?,
        @SerializedName("profile_index")
        val profileIndex: Int,
        @SerializedName("profiles")
        val profiles: List<TerracottaProfile>?
    ) : Ready(port, index, state) {

        override fun localStringRes(): Int = R.string.terracotta_status_host_ok

        override fun isForkOf(state: TerracottaState?): Boolean =
            state is HostOK && (this.index - state.index) <= profileIndex
    }

    @Keep
    class GuestConnecting(port: Int, index: Int, state: String) : Ready(port, index, state) {
        override fun localStringRes(): Int = R.string.terracotta_status_guest_starting
    }

    @Keep
    class GuestStarting(
        port: Int,
        index: Int,
        state: String,
        @SerializedName("difficulty")
        val difficulty: Difficulty
    ) : Ready(port, index, state) {
        @Keep
        enum class Difficulty(val textRes: Int) {
            /** 不应该使用这个枚举的[textRes] */
            UNKNOWN(-1),
            EASIEST(R.string.terracotta_difficulty_easiest),
            SIMPLE(R.string.terracotta_difficulty_simple),
            MEDIUM(R.string.terracotta_difficulty_medium),
            TOUGH(R.string.terracotta_difficulty_tough)
        }

        override fun localStringRes(): Int = R.string.terracotta_status_guest_starting
    }

    @Keep
    class GuestOK(
        port: Int,
        index: Int,
        state: String,
        @SerializedName("url")
        val url: String?,
        @SerializedName("profile_index")
        val profileIndex: Int,
        @SerializedName("profiles")
        val profiles: List<TerracottaProfile>?
    ) : Ready(port, index, state) {

        override fun localStringRes(): Int = R.string.terracotta_status_guest_ok

        override fun isForkOf(state: TerracottaState?): Boolean =
            state is GuestOK && (this.index - state.index) <= profileIndex
    }

    @Keep
    class Exception(port: Int, index: Int, state: String, val type: Int) : Ready(port, index, state) {
        @Keep
        enum class Type(val textRes: Int) {
            PING_HOST_FAIL(R.string.terracotta_status_exception_desc_ping_host_fail),
            PING_HOST_RST(R.string.terracotta_status_exception_desc_ping_host_rst),
            GUEST_ET_CRASH(R.string.terracotta_status_exception_desc_guest_et_crash),
            HOST_ET_CRASH(R.string.terracotta_status_exception_desc_host_et_crash),
            PING_SERVER_RST(R.string.terracotta_status_exception_desc_ping_server_rst),
            SCAFFOLDING_INVALID_RESPONSE(R.string.terracotta_status_exception_desc_scaffolding_invalid_response)
        }

        override fun localStringRes(): Int = R.string.terracotta_status_exception

        fun getEnumType(): Type = Type.entries[type]
    }

    companion object {
        private fun createGson(): Gson = GsonBuilder()
            .registerTypeAdapterFactory(TerracottaStateTypeAdapterFactory())
            .create()

        val TerracottaStateGson = createGson()
    }
}

class TerracottaStateTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (!TerracottaState.Ready::class.java.isAssignableFrom(rawType)) return null

        val waitingAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.Waiting::class.java))
        val hostScanningAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.HostScanning::class.java))
        val hostStartingAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.HostStarting::class.java))
        val hostOKAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.HostOK::class.java))
        val guestConnectingAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.GuestConnecting::class.java))
        val guestStartingAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.GuestStarting::class.java))
        val guestOKAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.GuestOK::class.java))
        val exceptionAdapter = gson.getDelegateAdapter(this, TypeToken.get(TerracottaState.Exception::class.java))

        @Suppress("UNCHECKED_CAST")
        return object : TypeAdapter<TerracottaState.Ready>() {
            override fun write(out: JsonWriter, value: TerracottaState.Ready) {
                when (value) {
                    is TerracottaState.Waiting -> waitingAdapter.write(out, value)
                    is TerracottaState.HostScanning -> hostScanningAdapter.write(out, value)
                    is TerracottaState.HostStarting -> hostStartingAdapter.write(out, value)
                    is TerracottaState.HostOK -> hostOKAdapter.write(out, value)
                    is TerracottaState.GuestConnecting -> guestConnectingAdapter.write(out, value)
                    is TerracottaState.GuestStarting -> guestStartingAdapter.write(out, value)
                    is TerracottaState.GuestOK -> guestOKAdapter.write(out, value)
                    is TerracottaState.Exception -> exceptionAdapter.write(out, value)
                }
            }

            override fun read(reader: JsonReader): TerracottaState.Ready {
                val jsonElement = JsonParser.parseReader(reader).asJsonObject
                val stateName = jsonElement.get("state")?.asString

                val result: TerracottaState.Ready = when (stateName) {
                    "waiting" -> waitingAdapter.fromJsonTree(jsonElement)
                    "host-scanning" -> hostScanningAdapter.fromJsonTree(jsonElement)
                    "host-starting" -> hostStartingAdapter.fromJsonTree(jsonElement)
                    "host-ok" -> hostOKAdapter.fromJsonTree(jsonElement)
                    "guest-connecting" -> guestConnectingAdapter.fromJsonTree(jsonElement)
                    "guest-starting" -> guestStartingAdapter.fromJsonTree(jsonElement)
                    "guest-ok" -> guestOKAdapter.fromJsonTree(jsonElement)
                    "exception" -> exceptionAdapter.fromJsonTree(jsonElement)
                    else -> throw JsonParseException("Unknown state type: $stateName")
                }

                //统一进行校验
                validateResult(result)

                return result
            }
        } as TypeAdapter<T>
    }

    private fun validateResult(result: TerracottaState.Ready) {
        when (result) {
            is TerracottaState.HostOK -> {
                require(result.code != null) { "HostOK.code is null" }
                require(result.profiles != null) { "HostOK.profiles is null" }
            }
            is TerracottaState.GuestOK -> {
                require(result.profiles != null) { "GuestOK.profiles is null" }
            }
            is TerracottaState.Exception -> {
                val type = result.type
                val max = Type.entries.size
                require(type in 0 until max) { "Exception.type=$type must be in [0, $max)" }
            }
            else -> {}
        }
    }
}
