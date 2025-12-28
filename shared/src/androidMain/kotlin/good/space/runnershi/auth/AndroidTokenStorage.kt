package good.space.runnershi.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidTokenStorage(
    context: Context
) : TokenStorage {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "token_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String?) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override suspend fun clearTokens() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) {
                remove(KEY_ACCESS_TOKEN)
                    .remove(KEY_REFRESH_TOKEN)
            } // apply() 대신 commit() 사용하여 동기적으로 완료 보장
        }
    }
}
