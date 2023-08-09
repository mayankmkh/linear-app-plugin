package dev.mayankmkh.intellij.linear

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import dev.mayankmkh.intellij.linear.apolloGenerated.type.DateTime
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

private class AuthorizationInterceptor(private val apiKeyProvider: ApiKeyProvider) : HttpInterceptor {
    override suspend fun intercept(request: HttpRequest, chain: HttpInterceptorChain): HttpResponse {
        val authorizedRequest = request.newBuilder()
            .addHeader("Authorization", apiKeyProvider.getApiKey())
            .build()
        return chain.proceed(authorizedRequest)
    }
}

internal fun createApolloClient(serverUrl: String, apiKeyProvider: ApiKeyProvider) = ApolloClient.Builder()
    .serverUrl(serverUrl)
    .addCustomScalarAdapter(DateTime.type, dateCustomTypeAdapter)
    .addHttpInterceptor(AuthorizationInterceptor(apiKeyProvider))
    .build()

fun interface ApiKeyProvider {
    fun getApiKey(): String
}
