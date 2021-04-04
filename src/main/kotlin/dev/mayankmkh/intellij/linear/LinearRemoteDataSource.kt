package dev.mayankmkh.intellij.linear

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.intellij.util.containers.map2Array
import dev.mayankmkh.intellij.linear.models.LinearTask
import kotlinx.coroutines.runBlocking
import java.util.logging.Logger

class LinearRemoteDataSource(private val apolloClient: ApolloClient) {

    fun getIssues(query: String?, offset: Int, limit: Int, withClosed: Boolean): Array<LinearTask> {
        LOG.info("query: $query, offset: $offset, limit: $limit, withClosed: $withClosed")
        val response = runBlocking { apolloClient.query(IssuesQuery("MO")).await() }
        return response.data?.team?.issues?.nodes?.map2Array { LinearTask(it) } ?: emptyArray()
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(LinearRemoteDataSource::class.simpleName)
    }
}
