package site.doramusic.app.http

import dora.util.CryptoUtils
import java.security.SecureRandom

object SecureRequestBuilder {

    const val AES_KEY_LENGTH = 16 // bytes (128位)
    const val RSA_PUBLIC = ""   // 等待公钥...

    enum class SecureMode {
        NONE,
        ENC,
        ENC_SIGN
    }

    /**
     * 获取随机key。
     */
    @JvmStatic
    fun getRandomKey(): String {
        val sb = StringBuilder(AES_KEY_LENGTH)
        val random = SecureRandom()
        repeat(AES_KEY_LENGTH) {
            when (random.nextInt(3)) {
                0 -> {
                    // 0-9
                    sb.append(random.nextInt(10))
                }
                1 -> {
                    // A-Z
                    sb.append((random.nextInt(26) + 'A'.code).toChar())
                }
                2 -> {
                    // a-z
                    sb.append((random.nextInt(26) + 'a'.code).toChar())
                }
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun build(
        req: BaseReq,
        mode: SecureMode
    ): ReqBody? {
        return when (mode) {
            // 明文
            SecureMode.NONE -> {
                ReqBody(
                    mode = "NONE",
                    data = req.payload
                )
            }
            // 端到端加密
            SecureMode.ENC -> {
                val aesKey = getRandomKey()
                ReqBody(
                    mode = "ENC",
                    key = CryptoUtils.encryptByPublic(RSA_PUBLIC, aesKey),
                    data = CryptoUtils.encryptAES(aesKey, req.payload)
                )
            }
            // 端到端加密 + 客户端签名
            SecureMode.ENC_SIGN -> {
                // 不告诉你，这个项目不提供可信客户端能力
                null
            }
        }
    }
}