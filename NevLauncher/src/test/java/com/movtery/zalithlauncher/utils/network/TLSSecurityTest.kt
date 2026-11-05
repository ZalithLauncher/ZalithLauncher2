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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class TLSSecurityTest {
    private fun resource(name: String) = "/com/movtery/zalithlauncher/utils/network/$name"

    private fun loadCertificates(resource: String): List<X509Certificate> {
        val factory = CertificateFactory.getInstance("X.509")
        return this::class.java.getResourceAsStream(resource)!!.use { stream ->
            factory.generateCertificates(stream).map { it as X509Certificate }
        }
    }

    /** 始终放行的假委托，用于观察包装器是否把校验交给了平台信任管理器 */
    private class PermissiveTrustManager(var delegated: Boolean = false) : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            delegated = true
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            delegated = true
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    @Test
    fun blocksAllPublishedCertificates() {
        val certificates = loadCertificates(resource("compromised.pem"))
        assertEquals(16, certificates.size)

        //公钥指纹须与 HMCL 测试固化的 8 把被泄露公钥完全一致（公钥 DER 编码的 SHA-256）
        val fingerprints = certificates.mapTo(HashSet()) {
            MessageDigest.getInstance("SHA-256").digest(it.publicKey.encoded).toHexString()
        }
        assertEquals(
            setOf(
                "ebed6475c2c2d80fcb199f3fe8dd6e6b3ce60b77eb15d4e42c3dec0c6f5b5105", // EM DV TLS CA - G3B
                "43bffa19d6ef651451f6841e019931e96f6f2eaeff28ab4b6f1ce4576de2717b", // EM OV TLS CA - G3B
                "e0254c7fcdc265e2f0f8a428dabb75ab4ae039b4872d62f5ee57f0877d5a8a41", // EM EV TLS CA - G3B
                "49dd353dc2b919a3770645117d0e60c16115e7d1bc259eefe734b00ea434fe5f", // EM DV TLS CA - G3C
                "63ecefee431d49523bc1136c4e72d0206a477086caf606cd5cf025686c109cf8", // EM OV TLS CA - G3C
                "5c0660a2b164910d8ff589c18e14552e9e0ec237dcefe2cb736d41a4811f81a0", // EM EV TLS CA - G3C
                "ec4ffa1ad0ac88081314b7aaffef6a91f97cd57291bb5ba686c5415f5eb4f6b6", // EM OV CS CA - G3B
                "92c063d67e8f14db71d288e11266ffdaf69cbf115f7bab14f3bc2f68d25e0e30"  // EM OV CS CA - G3C
            ),
            fingerprints
        )
        certificates.forEach { assertTrue(TLSSecurity.isBlockedCertificate(it)) }
    }

    @Test
    fun eMudhraRootsAreNotBlocked() {
        //被泄露的是中间 CA 的私钥，eMudhra 根证书本身不受影响，不应被拦截
        val certificates = loadCertificates(resource("roots.pem"))
        assertEquals(4, certificates.size)
        certificates.forEach { assertFalse(TLSSecurity.isBlockedCertificate(it)) }
    }

    @Test
    fun rejectsPublishedTlsChains() {
        //we-have-a-problem.com 发布的真实攻击链样例：aaa 变体为叶证书 + 交叉签名的被泄露中间证书，其余多一级签发链
        val compromised = loadCertificates(resource("compromised.pem"))
        for (type in listOf("dv", "ov", "ev")) {
            for (generation in listOf("g3b", "g3c")) {
                for (crossSign in listOf("", "-aaa")) {
                    val name = "$type-$generation$crossSign-emudhra.we-have-a-problem.com.pem"
                    val chain = loadCertificates(resource(name))
                    assertEquals("chain size of $name", if (crossSign.isEmpty()) 3 else 2, chain.size)

                    //首张为叶证书，本身无辜，不应被拦截
                    val leaf = chain.first()
                    assertFalse("leaf of $name must not be blocked", TLSSecurity.isBlockedCertificate(leaf))
                    //叶证书确实由被泄露的 CA 签发
                    assertTrue(
                        "$name is not issued by a compromised CA",
                        compromised.any { it.subjectX500Principal == leaf.issuerX500Principal }
                    )
                    //链上存在被泄露公钥，整条链必须拒绝
                    assertTrue(
                        "$name does not contain a blocked certificate",
                        chain.drop(1).any { TLSSecurity.isBlockedCertificate(it) }
                    )
                    val wrapper = TLSSecurity.ChainCheckingTrustManager(PermissiveTrustManager())
                    assertThrows(CertificateException::class.java) {
                        wrapper.checkServerTrusted(chain.toTypedArray(), "ECDHE_ECDSA")
                    }
                }
            }
        }
    }

    @Test
    fun chainWithBlockedKeyIsRejected() {
        val compromised = loadCertificates(resource("compromised.pem")).first()
        val wrapper = TLSSecurity.ChainCheckingTrustManager(PermissiveTrustManager())

        assertThrows(CertificateException::class.java) {
            wrapper.checkServerTrusted(arrayOf(compromised), "ECDHE_ECDSA")
        }
        assertThrows(CertificateException::class.java) {
            wrapper.checkClientTrusted(arrayOf(compromised), "ECDHE_ECDSA")
        }
    }

    @Test
    fun cleanChainIsDelegatedToPlatform() {
        val root = loadCertificates(resource("roots.pem")).first()
        val delegate = PermissiveTrustManager()
        val wrapper = TLSSecurity.ChainCheckingTrustManager(delegate)

        wrapper.checkServerTrusted(arrayOf(root), "RSA")
        wrapper.checkClientTrusted(arrayOf(root), "RSA")
        assertTrue(delegate.delegated)
    }

    @Test
    fun acceptedIssuersExcludeBlockedKeys() {
        val compromised = loadCertificates(resource("compromised.pem")).first()
        val root = loadCertificates(resource("roots.pem")).first()
        val delegate = object : X509TrustManager by PermissiveTrustManager() {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf(compromised, root)
        }
        val wrapper = TLSSecurity.ChainCheckingTrustManager(delegate)

        assertArrayEquals(arrayOf(root), wrapper.acceptedIssuers)
    }
}
