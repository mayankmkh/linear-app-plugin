package dev.mayankmkh.intellij.linear

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import dev.mayankmkh.intellij.linear.type.CustomType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

private val dateCustomTypeAdapter = object : CustomTypeAdapter<Date> {
    override fun decode(value: CustomTypeValue<*>): Date {
        val temporalAccessor = DateTimeFormatter.ISO_INSTANT.parse(value.value.toString())
        val instant = Instant.from(temporalAccessor)
        return Date.from(instant)
    }

    override fun encode(value: Date): CustomTypeValue<*> {
        val dateString = DateTimeFormatter.ISO_INSTANT.format(value.toInstant())
        return CustomTypeValue.GraphQLString(dateString)
    }
}

private class AuthorizationInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", apiKey)
            .build()
        return chain.proceed(request)
    }
}

internal fun createApolloClient(serverUrl: String, apiKey: String) = ApolloClient.builder()
    .serverUrl(serverUrl)
    .addCustomTypeAdapter(CustomType.DATETIME, dateCustomTypeAdapter)
    .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(apiKey))
            .build()
    )
    .build()
