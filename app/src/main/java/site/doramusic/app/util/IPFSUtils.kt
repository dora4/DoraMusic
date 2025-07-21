package site.doramusic.app.util

import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import site.doramusic.app.util.UCANUtils.generateEd25519KeyPair
import site.doramusic.app.util.UCANUtils.generateUCAN
import java.io.File
import java.io.IOException

object IPFSUtils {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun uploadToWeb3Storage(file: File, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .build()
        val keyPair = generateEd25519KeyPair()
        val privateKeyBytes: ByteArray = (keyPair.private as Ed25519PrivateKeyParameters).encoded
        val ucanToken = generateUCAN(privateKeyBytes, "did:key:z6MkmibVdapVvfV41EK7VVtCnYnQ1xmxFh5ihnhLGK55Ak8o", "did:web:web3.storage")
        val request = Request.Builder()
            .url("https://api.web3.storage/upload")
            .addHeader("Authorization", "Bearer $ucanToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "Upload failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // 返回 JSON 示例：
                    // {
                    //   "cid": "bafybeifg6d..."
                    // }
                    val cid = Regex("\"cid\"\\s*:\\s*\"(.*?)\"").find(responseBody ?: "")?.groupValues?.get(1)
                    if (cid != null) {
                        onSuccess(cid)
                    } else {
                        onError("CID not found in response")
                    }
                } else {
                    onError("Upload failed: ${response.code}")
                }
            }
        })
    }
}