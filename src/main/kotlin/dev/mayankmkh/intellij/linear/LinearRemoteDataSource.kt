package dev.mayankmkh.intellij.linear

import apolloGenerated.dev.mayankmkh.intellij.linear.GetIssueStatesQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.GetPageInfoQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.IssuesQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.TestConnectionQuery
import apolloGenerated.dev.mayankmkh.intellij.linear.UpdateIssueStateMutation
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
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
    ): List<IssuesQuery.Node> {
        // FIXME: 04/04/21 Ignoring query and withClosed for now
        LOG.info("query: $query, offset: $offset, limit: $limit, withClosed: $withClosed")
        return runBlocking { getIssues(teamId, offset, limit) }
    }

    private suspend fun getIssues(teamId: String, offset: Int, limit: Int): List<IssuesQuery.Node> =
        withContext(Dispatchers.IO) {
            var pageInfo = getIssuePageInfo(teamId, offset)
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
            list
        }

    private suspend fun getIssuePageInfo(teamId: String, offset: Int): IssuesQuery.PageInfo {
        val getPageInfo = getPageInfo(teamId, offset)
        return if (getPageInfo != null) {
            IssuesQuery.PageInfo(hasNextPage = getPageInfo.hasNextPage, endCursor = getPageInfo.endCursor)
        } else {
            IssuesQuery.PageInfo(hasNextPage = true, endCursor = null)
        }
    }

    private suspend fun getPageInfo(teamId: String, offset: Int): GetPageInfoQuery.PageInfo? =
        withContext(Dispatchers.IO) {
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
