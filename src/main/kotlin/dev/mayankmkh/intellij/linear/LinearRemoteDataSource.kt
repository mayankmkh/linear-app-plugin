package dev.mayankmkh.intellij.linear

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.intellij.util.containers.map2Array
import dev.mayankmkh.intellij.linear.models.LinearTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class LinearRemoteDataSource(private val teamId: String, private val apolloClient: ApolloClient) {

    fun getIssues(query: String?, offset: Int, limit: Int, withClosed: Boolean): Array<LinearTask> {
        // FIXME: 04/04/21 Ignoring query and withClosed for now
        LOG.info("query: $query, offset: $offset, limit: $limit, withClosed: $withClosed")
        return runBlocking { getIssues(offset, limit) }
    }

    private suspend fun getIssues(offset: Int, limit: Int): Array<LinearTask> = withContext(Dispatchers.IO) {
        var pageInfo = getIssuePageInfo(offset)
        LOG.info("pageInfo: $pageInfo")

        var remainingIssues = limit
        val list: MutableList<IssuesQuery.Node> = ArrayList(limit)

        while (remainingIssues > 0 && pageInfo.hasNextPage) {
            LOG.info("remainingIssues: $remainingIssues")
            val numberOfItems = remainingIssues.coerceAtMost(BATCH_SIZE)
            val response =
                apolloClient.query(IssuesQuery(teamId, numberOfItems, Input.optional(pageInfo.endCursor))).await()

            val data = response.data ?: break
            val issues = data.team.issues
            val nodes = issues.nodes

            list.addAll(nodes)
            pageInfo = issues.pageInfo
            remainingIssues -= nodes.size
            LOG.info("pageInfo: $pageInfo")
        }

        LOG.info("list: " + list.joinToString { it.identifier })
        list.map2Array { LinearTask(it) }
    }

    private suspend fun getIssuePageInfo(offset: Int): IssuesQuery.PageInfo {
        val getPageInfo = getPageInfo(offset)
        return if (getPageInfo != null) {
            IssuesQuery.PageInfo(hasNextPage = getPageInfo.hasNextPage, endCursor = getPageInfo.endCursor)
        } else {
            IssuesQuery.PageInfo(hasNextPage = true, endCursor = null)
        }
    }

    private suspend fun getPageInfo(offset: Int): GetPageInfoQuery.PageInfo? = withContext(Dispatchers.IO) {
        var pendingOffset = offset
        val firstPageInfo = GetPageInfoQuery.PageInfo(hasNextPage = true, endCursor = null)
        var pageInfo = firstPageInfo

        while (pendingOffset > 0 && pageInfo.hasNextPage) {
            val pageOffset = pendingOffset.coerceAtMost(MAX_COUNT)
            val getPageInfoQuery = GetPageInfoQuery(teamId, pageOffset, Input.optional(pageInfo.endCursor))
            val response = apolloClient.query(getPageInfoQuery).await()
            val data = response.data ?: break
            pageInfo = data.team.issues.pageInfo
            pendingOffset -= pageOffset
        }

        if (pageInfo === firstPageInfo) null else pageInfo
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(LinearRemoteDataSource::class.simpleName)
        private const val BATCH_SIZE = 50
        private const val MAX_COUNT = 250
    }
}
