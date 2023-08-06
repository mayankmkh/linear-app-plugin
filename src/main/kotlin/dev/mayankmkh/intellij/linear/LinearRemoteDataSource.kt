package dev.mayankmkh.intellij.linear

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Query
import com.intellij.tasks.CustomTaskState
import com.intellij.tasks.Task
import dev.mayankmkh.intellij.linear.apolloGenerated.GetIssueStatesQuery
import dev.mayankmkh.intellij.linear.apolloGenerated.GetPageInfoQuery
import dev.mayankmkh.intellij.linear.apolloGenerated.GetSearchIssuesPageInfoQuery
import dev.mayankmkh.intellij.linear.apolloGenerated.IssuesQuery
import dev.mayankmkh.intellij.linear.apolloGenerated.SearchIssuesQuery
import dev.mayankmkh.intellij.linear.apolloGenerated.TestConnectionQuery
import dev.mayankmkh.intellij.linear.apolloGenerated.UpdateIssueStateMutation
import dev.mayankmkh.intellij.linear.apolloGenerated.fragment.PageInfoIssueConnection
import dev.mayankmkh.intellij.linear.apolloGenerated.fragment.ShortIssueConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.logging.Logger

class LinearRemoteDataSource(private val apolloClient: ApolloClient) {

    fun getIssues(
        teamId: String,
        query: String?,
        offset: Int,
        limit: Int,
        withClosed: Boolean
    ): List<ShortIssueConnection.Node> {
        // FIXME: 04/04/21 Ignoring withClosed for now
        LOG.info("query: $query, offset: $offset, limit: $limit, withClosed: $withClosed")
        return try {
            runBlocking { getIssues(teamId, query, offset, limit) }
        } catch (e: InterruptedException) {
            emptyList()
        }
    }

    private suspend fun getIssues(
        teamId: String,
        query: String?,
        offset: Int,
        limit: Int
    ): List<ShortIssueConnection.Node> {
        return if (query.isNullOrBlank()) {
            val pageInfo = getIssuesPageInfo(teamId, offset)
            getIssuesInternal(
                limit,
                pageInfo,
                createQuery = { numberOfItems, endCursor -> IssuesQuery(teamId, numberOfItems, endCursor) },
                getShortIssueConnection = { it.team.issues.shortIssueConnection }
            )
        } else {
            val pageInfo = getSearchIssuesPageInfo(teamId, query, offset)
            getIssuesInternal(
                limit,
                pageInfo,
                createQuery = { numberOfItems, endCursor ->
                    SearchIssuesQuery(
                        query,
                        teamId,
                        numberOfItems,
                        endCursor
                    )
                },
                getShortIssueConnection = { it.issueSearch.shortIssueConnection }
            )
        }
    }

    private suspend fun <D : Query.Data> getIssuesInternal(
        limit: Int,
        initialIssuePageInfo: PageInfoIssueConnection.PageInfo?,
        createQuery: (offset: Int, endCursor: Optional<String>) -> Query<D>,
        getShortIssueConnection: (data: D) -> ShortIssueConnection
    ) = withContext(Dispatchers.IO) {
        var pageInfo = initialIssuePageInfo ?: PageInfoIssueConnection.PageInfo(hasNextPage = true, endCursor = null)
        LOG.info("pageInfo: $pageInfo")

        var remainingIssues = limit
        val list: MutableList<ShortIssueConnection.Node> = ArrayList(limit)

        while (remainingIssues > 0 && pageInfo.hasNextPage) {
            LOG.info("remainingIssues: $remainingIssues")
            val numberOfItems = remainingIssues.coerceAtMost(BATCH_SIZE)
            val issuesQuery = createQuery(numberOfItems, Optional.presentIfNotNull(pageInfo.endCursor))
            val response = apolloClient.query(issuesQuery).execute()

            val data = response.data ?: break
            val shortIssueConnection = getShortIssueConnection(data)
            val nodes = shortIssueConnection.nodes

            list.addAll(nodes)
            pageInfo = shortIssueConnection.pageInfoIssueConnection.pageInfo
            remainingIssues -= nodes.size
            LOG.info("pageInfo: $pageInfo")
        }

        LOG.info("list: " + list.joinToString { it.identifier })
        list
    }

    private suspend fun getIssuesPageInfo(teamId: String, offset: Int): PageInfoIssueConnection.PageInfo? {
        return getPageInfoInternal(
            offset,
            createQuery = { pageOffset, endCursor -> GetPageInfoQuery(teamId, pageOffset, endCursor) },
            getPageInfoIssueConnection = { it.team.issues.pageInfoIssueConnection }
        )
    }

    private suspend fun getSearchIssuesPageInfo(
        teamId: String,
        query: String,
        offset: Int
    ): PageInfoIssueConnection.PageInfo? {
        return getPageInfoInternal(
            offset,
            createQuery = { pageOffset, endCursor ->
                GetSearchIssuesPageInfoQuery(
                    query,
                    teamId,
                    pageOffset,
                    endCursor
                )
            },
            getPageInfoIssueConnection = { it.issueSearch.pageInfoIssueConnection }
        )
    }

    private suspend fun <D : Query.Data> getPageInfoInternal(
        startOffset: Int,
        createQuery: (offset: Int, endCursor: Optional<String>) -> Query<D>,
        getPageInfoIssueConnection: (data: D) -> PageInfoIssueConnection
    ): PageInfoIssueConnection.PageInfo? = withContext(Dispatchers.IO) {
        var pendingOffset = startOffset
        val firstPageInfo = PageInfoIssueConnection.PageInfo(hasNextPage = true, endCursor = null)
        var pageInfo = firstPageInfo

        while (pendingOffset > 0 && pageInfo.hasNextPage) {
            val pageOffset = pendingOffset.coerceAtMost(MAX_COUNT)
            val getPageInfoQuery = createQuery(pageOffset, Optional.presentIfNotNull(pageInfo.endCursor))
            val response = apolloClient.query(getPageInfoQuery).execute()
            val data = response.data ?: break
            pageInfo = getPageInfoIssueConnection(data).pageInfo
            pendingOffset -= pageOffset
        }

        if (pageInfo === firstPageInfo) null else pageInfo
    }

    suspend fun testConnection(teamId: String) = withContext(Dispatchers.IO) {
        val response = apolloClient.query(TestConnectionQuery(teamId)).execute()
        response.errors?.getOrNull(0)?.let {
            throw IllegalArgumentException(it.message)
        }
    }

    suspend fun getAvailableTaskStates(task: Task): MutableSet<CustomTaskState> = withContext(Dispatchers.IO) {
        val response = apolloClient.query(GetIssueStatesQuery(task.id)).execute()
        response.errors?.getOrNull(0)?.let {
            throw IllegalArgumentException(it.message)
        }
        response.data?.issue?.team?.states?.nodes?.map { CustomTaskState(it.id, it.name) }?.toMutableSet()
            ?: mutableSetOf()
    }

    suspend fun setTaskState(task: Task, state: CustomTaskState): Unit = withContext(Dispatchers.IO) {
        val response = apolloClient.mutation(UpdateIssueStateMutation(task.id, state.id)).execute()
        response.errors?.getOrNull(0)?.let {
            throw IllegalArgumentException(it.message)
        }
        check(response.data?.issueUpdate?.success == true) {
            "State could not be updated for Task ${task.id} to ${state.presentableName}"
        }
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(LinearRemoteDataSource::class.simpleName)
        private const val BATCH_SIZE = 50
        private const val MAX_COUNT = 250
    }
}
