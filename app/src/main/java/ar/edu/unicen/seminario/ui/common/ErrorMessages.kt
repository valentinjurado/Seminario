package ar.edu.unicen.seminario.ui.common

import androidx.annotation.StringRes
import ar.edu.unicen.seminario.R
import retrofit2.HttpException
import java.io.IOException

@StringRes
fun Throwable.toUserMessage(): Int = when (this) {
    is HttpException -> if (code() == 401) R.string.error_token else R.string.error_server
    is IOException -> R.string.error_no_internet
    else -> R.string.error_unknown
}
