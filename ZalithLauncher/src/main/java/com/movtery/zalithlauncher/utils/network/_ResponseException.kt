package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode

fun ResponseException.toLocal(): AndroidStringText {
    val statusCode = response.status
    val codeString = statusCode.value.toString()
    // 把服务端返回的错误内容一起显示，否则 400 只能看到一句无用的通用提示
    val serverDetail = message
        ?.substringAfter("Text: \"", "")
        ?.substringBeforeLast("\"")
        ?.take(400)
        ?.trim()
        .orEmpty()
    if (serverDetail.isNotEmpty()) {
        return androidText("($codeString) $serverDetail")
    }
    val textRes = when (statusCode) {
        HttpStatusCode.BadRequest -> R.string.error_bad_request
        HttpStatusCode.Unauthorized -> R.string.error_unauthorized
        HttpStatusCode.Forbidden -> R.string.error_forbidden
        HttpStatusCode.NotFound -> R.string.error_notfound
        HttpStatusCode.NotAcceptable -> R.string.error_not_acceptable
        HttpStatusCode.RequestTimeout -> R.string.error_request_timeout
        HttpStatusCode.Conflict -> R.string.error_conflict
        HttpStatusCode.Gone -> R.string.error_gone
        HttpStatusCode.TooManyRequests -> R.string.error_too_many_requests
        HttpStatusCode.InternalServerError -> R.string.error_internal_server_error
        HttpStatusCode.BadGateway -> R.string.error_bad_gateway
        HttpStatusCode.ServiceUnavailable -> R.string.error_service_unavailable
        HttpStatusCode.GatewayTimeout -> R.string.error_gateway_timeout
        else -> return androidText(
            "($codeString) ${statusCode.description}"
        )
    }
    return androidText(textRes, codeString)
}

