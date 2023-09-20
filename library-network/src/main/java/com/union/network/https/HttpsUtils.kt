package com.union.network.https

import android.annotation.SuppressLint
import android.text.TextUtils
import com.union.network.utils.HttpLog
import java.io.IOException
import java.io.InputStream
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Https相关的工具类
 * @Author： VincenT
 * @Time： 2023/8/15 20:57
 */
object HttpsUtils {

    class SSLParams {
        lateinit  var sSLSocketFactory: SSLSocketFactory
        lateinit  var trustManager: X509TrustManager
    }

    /**
     * https单向认证
     * 用含有服务端公钥的证书校验服务端证书
     */
    @JvmStatic
    fun getSslSocketFactory(certificates: Array<out InputStream>): SSLParams {
        return getSslSocketFactory(null, null, certificates)
    }

    /**
     * https双向认证
     * bksFile 和 password -> 客户端使用bks证书校验服务端证书
     * certificates -> 用含有服务端公钥的证书校验服务端证书
     */
    fun getSslSocketFactory(bksFile: InputStream?, password: String?, certificates: Array<out InputStream>): SSLParams {
        val sslParams = SSLParams()
        val keyManagers = prepareKeyManager(bksFile, password)
        val trustManagers = prepareTrustManager(*certificates)
        return try {
            val sslContext = SSLContext.getInstance("TLS")
            val trustManager: X509TrustManager = if (trustManagers != null) {
                MyTrustManager(chooseTrustManager(trustManagers))
            } else {
                UnSafeTrustManager()
            }
            sslContext.init(keyManagers, arrayOf<TrustManager>(trustManager), null)
            sslParams.sSLSocketFactory = sslContext.socketFactory
            sslParams.trustManager = trustManager
            sslParams
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        } catch (e: KeyManagementException) {
            throw AssertionError(e)
        } catch (e: KeyStoreException) {
            throw AssertionError(e)
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    private fun prepareTrustManager(vararg certificates: InputStream): Array<TrustManager>? {
        if (certificates == null || certificates.isEmpty()) return null
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null)
            for ((index, certificate) in certificates.withIndex()) {
                val certificateAlias = index.toString()
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate))
                try {
                    certificate.close()
                } catch (e: IOException) {
                    HttpLog.e(e)
                }
            }
            val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            return trustManagerFactory.trustManagers
        } catch (e: NoSuchAlgorithmException) {
            HttpLog.e(e)
        } catch (e: CertificateException) {
            HttpLog.e(e)
        } catch (e: KeyStoreException) {
            HttpLog.e(e)
        } catch (e: Exception) {
            HttpLog.e(e)
        }
        return null
    }

    private fun prepareKeyManager(bksFile: InputStream?, password: String?): Array<KeyManager>? {
        try {
            if (bksFile == null || password == null) return null
            val clientKeyStore = KeyStore.getInstance("BKS")
            clientKeyStore.load(bksFile, password.toCharArray())
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(clientKeyStore, password.toCharArray())
            return keyManagerFactory.keyManagers
        } catch (e: KeyStoreException) {
            HttpLog.e(e)
        } catch (e: NoSuchAlgorithmException) {
            HttpLog.e(e)
        } catch (e: UnrecoverableKeyException) {
            HttpLog.e(e)
        } catch (e: CertificateException) {
            HttpLog.e(e)
        } catch (e: IOException) {
            HttpLog.e(e)
        } catch (e: Exception) {
            HttpLog.e(e)
        }
        return null
    }

    private fun chooseTrustManager(trustManagers: Array<TrustManager>): X509TrustManager? {
        for (trustManager in trustManagers) {
            if (trustManager is X509TrustManager) {
                return trustManager
            }
        }
        return null
    }


    @SuppressLint("CustomX509TrustManager")
    private class MyTrustManager(localTrustManager: X509TrustManager?) : X509TrustManager {
        private val defaultTrustManager: X509TrustManager?
        private val localTrustManager: X509TrustManager?

        init {
            val var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            var4.init(null as KeyStore?)
            defaultTrustManager = chooseTrustManager(var4.trustManagers)
            this.localTrustManager = localTrustManager
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            try {
                defaultTrustManager!!.checkServerTrusted(chain, authType)
            } catch (ce: CertificateException) {
                localTrustManager!!.checkServerTrusted(chain, authType)
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    }

    @SuppressLint("CustomX509TrustManager")
    private class UnSafeTrustManager : X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    /**
     * 此类是用于主机名验证的基接口。 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，
     * 则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。策略可以是基于证书的或依赖于其他验证方案。
     * 当验证 URL 主机名使用的默认规则失败时使用这些回调。如果主机名是可接受的，则返回 true
     */
    class DefaultHostnameVerifier : HostnameVerifier {
        private val verifyHostNameArray = arrayOf<String>()

        override fun verify(hostname: String, session: SSLSession): Boolean {
            return if (TextUtils.isEmpty(hostname)) {
                false
            } else !listOf(*verifyHostNameArray).contains(hostname)
        }
    }
}