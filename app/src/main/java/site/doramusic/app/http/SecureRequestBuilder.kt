package site.doramusic.app.http

import dora.util.CryptoUtils
import java.security.SecureRandom

object SecureRequestBuilder {

    const val AES_KEY_LENGTH = 16 // bytes (128位)
    const val RSA_PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0SGScqloAM1pJQ2ERUE+\n" +
            "xP/Dy+UY8e8a5HtLfVCQgpheQ64MHro1XuIhW4lTxw31EFurOqvZEHive9kJ4xy9\n" +
            "Ghw1hbrUfxKCig+g6naD13idePYHJ29M2LMw9JRzAwalDVl7RplEOxL25+cqJKjA\n" +
            "2Z6pwNrkLXaNYl6zPxW+TMD8tGQ0krWpt5+K5qiuTogJCTmmgDbUzcB0wQh0Hxcv\n" +
            "Uwo7GjjFN6dVXHHtJ1smrrFvKFDetGclDLJtsrgYGXUiyOKyTnEsKn/W31fPzL4s\n" +
            "kdJSpbqjU5ZpXvQewDx75A8MK5JUnBf8y9UOTKyUj0JmYdEyOD41rVCzgSTf4J3j\n" +
            "3wIDAQAB"  // RSA公钥

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