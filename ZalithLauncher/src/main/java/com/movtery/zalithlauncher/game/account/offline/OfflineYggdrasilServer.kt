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

package com.movtery.zalithlauncher.game.account.offline

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.jackhuang.hmcl.util.DigestUtils
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.Signature
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val TAG = "OfflineYggdrasil"

/**
 * 离线账号 Yggdrasil 服务器，用于本地加载玩家皮肤、披风
 * [Reference from HMCL](https://github.com/HMCL-dev/HMCL/blob/15e490f/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/offline/YggdrasilServer.java)
 */
class OfflineYggdrasilServer(
    private val port: Int = 0,
    val serverName: String = "${BuildKeys.LAUNCHER_IDENTIFIER}_Offline",
    val implementationName: String = BuildKeys.LAUNCHER_SHORT_NAME,
    val implementationVersion: String = "1.0"
) {
    private val charactersByUuid = ConcurrentHashMap<String, Character>()
    private val charactersByName = ConcurrentHashMap<String, Character>()
    private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply {
        initialize(2048)
    }.genKeyPair()

    private val serverStartedLatch = CountDownLatch(1)
    private var isServerRunning = false

    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    fun start() {
        server = embeddedServer(CIO, port = port) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            routing {
                suspend fun RoutingContext.runCatched(
                    block: suspend RoutingContext.() -> Unit
                ) {
                    runCatching {
                        block()
                    }.onFailure { e ->
                        Logger.error(TAG, "Internal server error", e)
                    }
                }

                get("/") {
                    runCatched { call.respondText(root(), ContentType.Application.Json) }
                }
                get("/status") {
                    runCatched { call.respondText(status(), ContentType.Application.Json) }
                }
                post("/api/profiles/minecraft") {
                    runCatched { call.respondText(profiles(call), ContentType.Application.Json) }
                }
                get("/sessionserver/session/minecraft/hasJoined") {
                    runCatched { call.respondText(hasJoined(call), ContentType.Application.Json) }
                }
                post("/sessionserver/session/minecraft/join") {
                    runCatched { call.respond(HttpStatusCode.NoContent) }
                }
                get("/sessionserver/session/minecraft/profile/{uuid}") {
                    runCatched { call.respondText(profile(call), ContentType.Application.Json) }
                }
                get("/textures/{hash}") {
                    runCatched { call.respond(texture(call)) }
                }
            }
        }.apply {
            monitor.subscribe(ApplicationStarted) {
                //服务器成功启动
                serverStartedLatch.countDown()
            }
        }

        server?.start(wait = false)

        //等待服务器启动完成
        val startTimeout = 10L //10秒超时
        if (serverStartedLatch.await(startTimeout, TimeUnit.SECONDS)) {
            isServerRunning = true
        }
    }

    fun stop() {
        isServerRunning = false
        server?.stop(1000, 5000)
        serverStartedLatch.countDown()
    }

    fun getPort(): Int? {
        if (!isServerRunning) {
            return null
        }

        val engine = server?.engine ?: return null
        return runBlocking {
            try {
                engine.resolvedConnectors().firstOrNull()?.port
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * 添加玩家角色
     * @param account 离线账号对象
     */
    fun addCharacter(account: Account) {
        val skinFile = account.getSkinFile()

        val skinBytes = skinFile.takeIf { it.exists() }?.readBytes()
        val skinHash = skinBytes?.let { DigestUtils.digestToString("SHA-256", it) }

        val character = Character(
            uuid = account.profileId.replace("-", ""),
            name = account.username,
            skin = LoadedSkin(
                skinHash = skinHash,
                skinBytes = skinBytes,
                model = account.skinModelType
            )
        )

        charactersByUuid[character.uuid.lowercase()] = character
        charactersByName[character.name.lowercase()] = character

        Logger.info(TAG, "Added character ${character.name} (${character.uuid}), skin hash = ${character.skin?.skinHash}")
    }





    private fun PublicKey.toPEMPublicKey(): String {
        val base64Key = Base64.getEncoder().encodeToString(this.encoded)
        return "-----BEGIN PUBLIC KEY-----\n$base64Key\n-----END PUBLIC KEY-----"
    }

    private fun root(): String {
        return buildJsonObject {
            put("skinDomains", JsonArray(listOf(JsonPrimitive("127.0.0.1"), JsonPrimitive("localhost"))))
            put("meta", buildJsonObject {
                put("serverName", JsonPrimitive(serverName))
                put("implementationName", JsonPrimitive(implementationName))
                put("implementationVersion", JsonPrimitive(implementationVersion))
                put("feature.non_email_login", JsonPrimitive(true))
            })
            put("signaturePublickey", JsonPrimitive(keyPair.public.toPEMPublicKey()))
        }.toString()
    }

    private fun status(): String {
        return buildJsonObject {
            put("user.count", JsonPrimitive(charactersByUuid.size))
            put("token.count", JsonPrimitive(0))
        }.toString()
    }

    private suspend fun profiles(call: ApplicationCall): String {
        val names = call.receive<List<String>>()
        return buildJsonArray {
            names.distinct().mapNotNull { charactersByName[it.lowercase()] }.forEach { character ->
                add(buildJsonObject {
                    put("id", JsonPrimitive(character.uuid))
                    put("name", JsonPrimitive(character.name))
                })
            }
        }.toString()
    }

    private fun hasJoined(call: ApplicationCall): String {
        val username = call.request.queryParameters["username"] ?: return buildJsonObject {
            put("error", JsonPrimitive("Missing username"))
        }.toString()
        Logger.debug(TAG, "Try find profile with username $username")

        val character = charactersByName[username.lowercase()] ?: return buildJsonObject { }.toString().also {
            Logger.debug(TAG, "Profile with username $username not found")
        }

        return character.toCompleteResponse("http://localhost:${getPort()}", this::sign).also {
            Logger.debug(TAG, "Found profile with username $username")
        }
    }

    private fun profile(call: ApplicationCall): String {
        val uuid = call.parameters["uuid"] ?: return buildJsonObject {
            put("error", JsonPrimitive("Missing uuid"))
        }.toString()
        Logger.debug(TAG, "Try find profile with uuid $uuid")

        val character = charactersByUuid[uuid.lowercase()] ?: return buildJsonObject { }.toString().also {
            Logger.debug(TAG, "Profile with uuid $uuid not found")
        }

        return character.toCompleteResponse("http://localhost:${getPort()}", this::sign).also {
            Logger.debug(TAG, "Found profile with uuid $uuid")
        }
    }

    private suspend fun texture(call: ApplicationCall) {
        val hash = call.parameters["hash"] ?: return call.respond(HttpStatusCode.NotFound)
        Logger.debug(TAG, "Try find skin with hash $hash")

        // 查找对应hash的皮肤或披风
        val match = charactersByUuid.values
            .firstNotNullOfOrNull { char ->
                when (hash) {
                    char.skin?.skinHash -> char.skin.skinBytes
                    char.skin?.capeHash -> char.skin.capeBytes
                    else -> null
                }
            }

        if (match != null) {
            Logger.debug(TAG, "Skin with hash $hash found")
            call.response.header("Cache-Control", "max-age=2592000, public")
            call.response.header("Etag", "\"$hash\"")
            call.respondBytes(match, ContentType.Image.PNG)
        } else {
            Logger.debug(TAG, "Skin with hash $hash not found")
            call.respond(HttpStatusCode.NotFound)
        }
    }

    /**
     * 签名工具
     */
    private fun sign(data: String): String {
        val signature = Signature.getInstance("SHA1withRSA")
        signature.initSign(keyPair.private)
        signature.update(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    /**
     * 玩家角色模型
     */
    data class Character(
        val uuid: String,
        val name: String,
        val skin: LoadedSkin? = null
    ) {
        fun toCompleteResponse(rootUrl: String, signer: (String) -> String): String {
            val texturesObject = buildJsonObject {
                put("timestamp", JsonPrimitive(System.currentTimeMillis()))
                put("profileId", JsonPrimitive(uuid))
                put("profileName", JsonPrimitive(name))
                put("textures", buildJsonObject {
                    skin?.skinHash?.let { hash ->
                        put("SKIN", buildJsonObject {
                            put("url", JsonPrimitive("$rootUrl/textures/$hash"))
                            //仅在玩家模型为细臂时，才会存在metadata字段，否则为粗臂
                            //Wiki：https://zh.minecraft.wiki/w/Mojang_API#%E8%8E%B7%E5%8F%96%E7%8E%A9%E5%AE%B6%E7%9A%84%E7%9A%AE%E8%82%A4%E5%92%8C%E6%8A%AB%E9%A3%8E
                            if (skin.model == SkinModelType.ALEX) {
                                put("metadata", buildJsonObject {
                                    put("model", JsonPrimitive("slim"))
                                })
                            }
                        })
                    }
                    skin?.capeHash?.let { hash ->
                        put("CAPE", buildJsonObject {
                            put("url", JsonPrimitive("$rootUrl/textures/$hash"))
                        })
                    }
                })
            }

            val jsonString = Json.encodeToString(texturesObject)
            val encoded = Base64.getEncoder().encodeToString(jsonString.toByteArray(Charsets.UTF_8))

            return buildJsonObject {
                put("id", JsonPrimitive(uuid))
                put("name", JsonPrimitive(name))
                put("properties", buildJsonArray {
                    add(buildJsonObject {
                        put("name", JsonPrimitive("textures"))
                        put("value", JsonPrimitive(encoded))
                        put("signature", JsonPrimitive(signer(encoded)))
                    })
                })
            }.toString()
        }
    }
}
