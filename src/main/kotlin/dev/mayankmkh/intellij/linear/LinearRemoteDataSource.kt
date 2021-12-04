package dev.mayankmkh.intellij.linear

import apolloGenerated.dev.mayankmkh.intellij.linear.GetIssueStatesQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.GetPageInfoQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.GetSearchIssuesPageInfoQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.IssuesQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.SearchIssuesQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.TestConnectionQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.UpdateIssueStateMutation
import apolloGenerated.dev.mayankmkh.intellij.linear.fragment.PageInfoIssueConnection
import apolloGenerated.dev.mayankmkh.intellij.linear.fragment.ShortIssueConnection
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.coroutines.await
import com.intellij.tasks.CustomTaskState
import com.intellij.tasks.Task
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
                getShortIssueConnection = { it.team.issues.fragments.shortIssueConnection }
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
                getShortIssueConnection = { it.issueSearch.fragments.shortIssueConnection }
            )
        }
    }

    private suspend fun <D : Operation.Data> getIssuesInternal(
        limit: Int,
        initialIssuePageInfo: PageInfoIssueConnection.PageInfo?,
        createQuery: (offset: Int, endCursor: Input<String>) -> Query<D, D, Operation.Variables>,
        getShortIssueConnection: (data: D) -> ShortIssueConnection
    ) = withContext(Dispatchers.IO) {
        var pageInfo = initialIssuePageInfo ?: PageInfoIssueConnection.PageInfo(hasNextPage = true, endCursor = null)
        LOG.info("pageInfo: $pageInfo")

        var remainingIssues = limit
        val list: MutableList<ShortIssueConnection.Node> = ArrayList(limit)

        while (remainingIssues > 0 && pageInfo.hasNextPage) {
            LOG.info("remainingIssues: $remainingIssues")
            val numberOfItems = remainingIssues.coerceAtMost(BATCH_SIZE)
            val issuesQuery = createQuery(numberOfItems, Input.optional(pageInfo.endCursor))
            val response = apolloClient.query(issuesQuery).await()

            val data = response.data ?: break
            val shortIssueConnection = getShortIssueConnection(data)
            val nodes = shortIssueConnection.nodes

            list.addAll(nodes)
            pageInfo = shortIssueConnection.fragments.pageInfoIssueConnection.pageInfo
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
            getPageInfoIssueConnection = { it.team.issues.fragments.pageInfoIssueConnection }
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
            getPageInfoIssueConnection = { it.issueSearch.fragments.pageInfoIssueConnection }
        )
    }

    private suspend fun <D : Operation.Data> getPageInfoInternal(
        startOffset: Int,
        createQuery: (offset: Int, endCursor: Input<String>) -> Query<D, D, Operation.Variables>,
        getPageInfoIssueConnection: (data: D) -> PageInfoIssueConnection
    ): PageInfoIssueConnection.PageInfo? = withContext(Dispatchers.IO) {
        var pendingOffset = startOffset
        val firstPageInfo = PageInfoIssueConnection.PageInfo(hasNextPage = true, endCursor = null)
        var pageInfo = firstPageInfo

        while (pendingOffset > 0 && pageInfo.hasNextPage) {
            val pageOffset = pendingOffset.coerceAtMost(MAX_COUNT)
            val getPageInfoQuery = createQuery(pageOffset, Input.optional(pageInfo.endCursor))
            val response = apolloClient.query(getPageInfoQuery).await()
            val data = response.data ?: break
            pageInfo = getPageInfoIssueConnection(data).pageInfo
            pendingOffset -= pageOffset
        }

        if (pageInfo === firstPageInfo) null else pageInfo
    }

    suspend fun testConnection(teamId: String) = withContext(Dispatchers.IO) {
        val response = apolloClient.query(TestConnectionQuery(teamId)).await()
        response.errors?.getOrNull(0)?.let {
            throw IllegalArgumentException(it.message)
        }
    }

    suspend fun getAvailableTaskStates(task: Task): MutableSet<CustomTaskState> = withContext(Dispatchers.IO) {
        val response = apolloClient.query(GetIssueStatesQuery(task.id)).await()
        response.errors?.getOrNull(0)?.let {
            throw IllegalArgumentException(it.message)
        }
        response.data?.issue?.team?.states?.nodes?.map { CustomTaskState(it.id, it.name) }?.toMutableSet()
            ?: mutableSetOf()
    }

    suspend fun setTaskState(task: Task, state: CustomTaskState): Unit = withContext(Dispatchers.IO) {
        val response = apolloClient.mutate(UpdateIssueStateMutation(task.id, state.id)).await()
        response.errors?.getOrNull(0)?.let {
            throw IllegalArgumentException(it.message)
        }
        if (response.data?.issueUpdate?.success == true) {
            // task updated successfully
        } else {
            throw IllegalStateException("State could not be updated for Task ${task.id} to ${state.presentableName}")
        }
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(LinearRemoteDataSource::class.simpleName)
        private const val BATCH_SIZE = 50
        private const val MAX_COUNT = 250
    }
}
