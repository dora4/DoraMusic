package site.doramusic.app.util

import android.os.Build
import android.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONArray
import org.json.JSONObject
import java.security.*
import java.security.spec.NamedParameterSpec

object UCANUtils {

    init {
        // 确保 BouncyCastle 可用
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    /**
     * 生成可用于 Web3.Storage 的 UCAN JWT。
     *
     * @param privateKeyBytes Ed25519 私钥 32字节
     * @param issuerDID 例如 "did:key:z6MkmibVdapVvfV41EK7VVtCnYnQ1xmxFh5ihnhLGK55Ak8o"
     * @param audienceDID "did:web:web3.storage"
     */
    fun generateUCAN(privateKeyBytes: ByteArray, issuerDID: String, audienceDID: String): String {
        val headerJson = JSONObject()
            .put("alg", "EdDSA")
            .put("typ", "JWT").toString()
        val now = System.currentTimeMillis() / 1000 // 单位：秒
        val expirationSeconds = 3600 // 3600秒，1小时
        val uploadPermission = JSONObject()
            .put("with", "ipfs://*")  // 允许上传所有 IPFS 资源
            .put("can", "upload")
            // 仅支持mp3和flac
            .put("fileTypes", JSONArray().put("mp3").put("flac"))
        val attArray = JSONArray().put(uploadPermission)
        val payloadJson = JSONObject()
            .put("iss", issuerDID) // 签发者 DID
            .put("aud", audienceDID) // 受众 DID
            .put("exp", now + expirationSeconds) // 过期时间
            .put("iat", now) // 签发时间
            .put("att", attArray) // 权限数组，空表示默认全部权限
            .put("nbf", now) // 生效时间
            .put("ucv", "0.7.0") // UCAN 版本
            .toString().trimIndent()

        val header = base64UrlEncode(headerJson.toByteArray())
        val payload = base64UrlEncode(payloadJson.toByteArray())
        val signingInput = "$header.$payload"

        val signature = signEd25519(privateKeyBytes, signingInput.toByteArray())
        val encodedSignature = base64UrlEncode(signature)

        return "$signingInput.$encodedSignature"
    }

    /**
     * Base64URL encode without padding。
     */
    private fun base64UrlEncode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * 使用 Ed25519 签名（使用 BouncyCastle）。
     */
    private fun signEd25519(privateKeyBytes: ByteArray, message: ByteArray): ByteArray {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // ✅ API 33+ (有 NamedParameterSpec)
                val spec = NamedParameterSpec("Ed25519")
                val keySpec = java.security.spec.EdECPrivateKeySpec(spec, privateKeyBytes)
                val privateKey = KeyFactory.getInstance("Ed25519").generatePrivate(keySpec)

                val signature = Signature.getInstance("Ed25519")
                signature.initSign(privateKey)
                signature.update(message)
                signature.sign()
            } else {
                // ✅ API 23 ~ 32 走 BouncyCastle
                val kf = KeyFactory.getInstance("Ed25519", "BC")
                val pkcs8 = convertRawEd25519PrivateKeyToPKCS8(privateKeyBytes)
                val keySpec = java.security.spec.PKCS8EncodedKeySpec(pkcs8)
                val privateKey = kf.generatePrivate(keySpec)

                val signature = Signature.getInstance("Ed25519", "BC")
                signature.initSign(privateKey)
                signature.update(message)
                signature.sign()
            }
        } catch (e: Exception) {
            throw RuntimeException("Ed25519 signing failed", e)
        }
    }

    /**
     * 将 32字节 raw Ed25519 私钥封装成 PKCS#8
     * 如果你的私钥来源已是 PKCS#8，可跳过此转换。
     */
    private fun convertRawEd25519PrivateKeyToPKCS8(rawKey: ByteArray): ByteArray {
        return Base64.decode("MC4CAQAwBQYDK2VwBCIEI" + Base64.encodeToString(rawKey, Base64.NO_WRAP), Base64.DEFAULT)
    }

    // 生成 Ed25519 密钥对（调用一次，保存私钥，后续调用用保存的私钥）
    fun generateEd25519KeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
        return kpg.generateKeyPair()
    }
}