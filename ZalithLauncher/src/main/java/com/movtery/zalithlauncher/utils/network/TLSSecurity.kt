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

package com.movtery.zalithlauncher.utils.network

import android.util.Log
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * 在系统默认信任库的基础上移除已确认泄露的 eMudhra CA 信任锚
 *
 * 参考 HMCL 的同类修复：
 * https://github.com/HMCL-dev/HMCL/commit/0d20910ea302a907d6a01fec9244ab10d2993d5e
 */
object TLSSecurity {
    private const val TAG = "TLSSecurity"

    /** 已泄露的 eMudhra CA 公钥，DER 编码 SubjectPublicKeyInfo 的小写十六进制串 */
    private val BLOCKED_KEYS: Set<String> = setOf(
        // EM DV TLS CA - G3B
        "3076301006072a8648ce3d020106052b8104002203620004e79ed9c04a9773c4735e51111da85ac2" +
                "bd5e64ab374b80e12cbc2038ec1e4f0e3b812fc9859e1e67afb04e2f8bc97f7485ba65aa911ee8a4" +
                "e45b62d5cc03d56be678b3c38cb4dc937ca2460e238453c892935d29d44f91e15606b63ccedab468",
        // EM OV TLS CA - G3B
        "3076301006072a8648ce3d020106052b810400220362000473897aa0eb3cfdca8b12402ffaf0abc4" +
                "e1212baf8dee778ac9ee2d6db83fac280c5e05638ae7a2acd71c322a8e361cdeb9f69c4302b9dd8d" +
                "73d1cc6caab2cc0aee4e216ce979dd518a4af4b626e5684cdd658037ed0c854e4cc12f4cf8646d0d",
        // EM EV TLS CA - G3B
        "3076301006072a8648ce3d020106052b810400220362000425dba85a64d397da0b1052e9ea546e48" +
                "7befea9c9d9d68a0c5eadcfeef75a69bb5d6fc01f42bfbca6eb40bcd64c0767e36ffd05d97a813c5" +
                "aa0c6c9530e79cf5f0866468f13806420aa5c851840b1e252540e790e4b654c3852ed64af1e8657d",
        // EM DV TLS CA - G3C
        "3076301006072a8648ce3d020106052b81040022036200043a7a7862c8c44d324ddd69033a6782c0" +
                "efeefe4055e68a123fa0371ba3bd9194514ee172a433e3630f02bd188037d9e5149f9b77db94a281" +
                "df987b2fea888b74bdfe8fda4c1099ee6ca19566fe3ef0e5f6b09a556ccc8ba50561df1b2a441d01",
        // EM OV TLS CA - G3C
        "3076301006072a8648ce3d020106052b8104002203620004138251cd52ac9298c1c8aad977321deb" +
                "97e709bd0b4ca0aca55dc8ad51dcfc9d1589a1597e3a5120e1efd631c63e1835cacae29869a62e16" +
                "31e8a28181ab56616dc45d918abc09f3ab0e63cf792aa4dced7387be37bba569549f1c02b270ed67",
        // EM EV TLS CA - G3C
        "3076301006072a8648ce3d020106052b81040022036200042d56e829a6fb82c64a4697d960af951e" +
                "9067ae91f926d34306da48a8aaeb7869b6a67e6d27426e0442aa03b36e1988dff636514ee1cf07f5" +
                "ba4b3ff2a06645ae7d31d5866ad31aa3d246eb0b3177ee99cb7566c516ce74808a668f91f0c47fca",
        // EM OV CS CA - G3B
        "3076301006072a8648ce3d020106052b8104002203620004aabb0fb0666acfcf01fad52bfcfd4c2b" +
                "f2cb3c3497de73950e5819b61fe695098741c8c491439b34a0431caffe5bf9b4c1c8e8ae0001f9f4" +
                "dc8ad82c59f3639e96cd6c3d4db0cefec26c3d62517b37761372743f8e8add77b10064f2abe5e050",
        // EM OV CS CA - G3C
        "3076301006072a8648ce3d020106052b8104002203620004b9f0e0634702a69ca6aadd443cfd62c4" +
                "1b9ba4b20924697d7974e7691a126fb96afb3e34c2fd3bc5f483061f265dc2a58436347323510436" +
                "b28f04f3862363067980e2503e02532d6546235a7165a259a1ef496f741b5fdee7787208e4b8faef"
    )

    private val hardened: Pair<SSLContext, X509TrustManager> by lazy { createHardened() }

    val sslSocketFactory: SSLSocketFactory get() = hardened.first.socketFactory
    val trustManager: X509TrustManager get() = hardened.second

    /**
     * 安装进程级默认 TLS 校验
     */
    fun install() {
        val (context, _) = hardened
        SSLContext.setDefault(context)
        HttpsURLConnection.setDefaultSSLSocketFactory(context.socketFactory)
    }

    /** 判断证书的公钥是否为已泄露的 CA 公钥 */
    fun isBlockedCertificate(certificate: X509Certificate): Boolean {
        val encoded = certificate.publicKey.encoded ?: return true
        return encoded.toHexString() in BLOCKED_KEYS
    }

    private fun createHardened(): Pair<SSLContext, X509TrustManager> {
        return try {
            val trustManager = ChainCheckingTrustManager(createPlatformTrustManager())
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf<TrustManager>(trustManager), null)
            context to trustManager
        } catch (failure: GeneralSecurityException) {
            Log.w(TAG, "TLS configuration failed; default TLS connections will be rejected", failure)
            try {
                val rejecting = RejectingTrustManager(failure)
                val context = SSLContext.getInstance("TLS")
                context.init(null, arrayOf<TrustManager>(rejecting), null)
                context to rejecting
            } catch (e: GeneralSecurityException) {
                throw IllegalStateException("Failed to disable unprotected TLS connections", e)
            }
        }
    }

    /**
     * 从平台默认信任管理器取回信任锚，剔除已泄露的公钥后重建信任库
     * @return 加固的信任管理器
     */
    private fun createPlatformTrustManager(): X509TrustManager {
        val defaults = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        defaults.init(null as KeyStore?)
        val systemManager = defaults.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
            ?: throw GeneralSecurityException("No X509TrustManager in the default trust manager factory")

        val store = KeyStore.getInstance(KeyStore.getDefaultType())
        store.load(null, null)
        systemManager.acceptedIssuers.forEachIndexed { index, certificate ->
            if (!isBlockedCertificate(certificate)) {
                store.setCertificateEntry("trusted-ca-$index", certificate)
            }
        }

        val hardenedFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        hardenedFactory.init(store)
        return hardenedFactory.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
            ?: throw GeneralSecurityException("No X509TrustManager in the hardened trust manager factory")
    }

    /**
     * 在平台 PKIX 校验通过后，再对整条证书链做被泄露公钥检查，
     * 覆盖信任锚移除后仍可能经交叉签名构造出的证书路径。
     */
    internal class ChainCheckingTrustManager(private val delegate: X509TrustManager) : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            delegate.checkClientTrusted(chain, authType)
            checkChain(chain)
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            delegate.checkServerTrusted(chain, authType)
            checkChain(chain)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> =
            delegate.acceptedIssuers.filterNot { isBlockedCertificate(it) }.toTypedArray()

        private fun checkChain(chain: Array<X509Certificate>) {
            for (certificate in chain) {
                if (isBlockedCertificate(certificate)) {
                    throw CertificateException(
                        "Certificate uses a compromised CA public key: ${certificate.subjectX500Principal}"
                    )
                }
            }
        }
    }

    /** TLS 配置失败时的降级信任管理器：拒绝一切证书认证 */
    private class RejectingTrustManager(private val failure: GeneralSecurityException) : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            throw CertificateException("TLS configuration failed", failure)
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            throw CertificateException("TLS configuration failed", failure)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
}
