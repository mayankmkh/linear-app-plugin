package dev.mayankmkh.intellij.linear

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.network.okHttpClient
import dev.mayankmkh.intellij.linear.apolloGenerated.type.DateTime
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

private val dateCustomTypeAdapter = object : Adapter<Date> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date {
        val temporalAccessor = DateTimeFormatter.ISO_INSTANT.parse(reader.nextString())
        val instant = Instant.from(temporalAccessor)
        return Date.from(instant)
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date) {
        val dateString = DateTimeFormatter.ISO_INSTANT.format(value.toInstant())
        writer.value(dateString)
    }
}

private class AuthorizationInterceptor(private val apiKeyProvider: ApiKeyProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", apiKeyProvider.getApiKey())
            .build()
        return chain.proceed(request)
    }
}

internal fun createApolloClient(serverUrl: String, apiKeyProvider: ApiKeyProvider) = ApolloClient.Builder()
    .serverUrl(serverUrl)
    .addCustomScalarAdapter(DateTime.type, dateCustomTypeAdapter)
    .okHttpClient(
        OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(apiKeyProvider))
            .build()
    )
    .build()

fun interface ApiKeyProvider {
    fun getApiKey(): String
}
