package dev.mayankmkh.intellij.linear

import com.intellij.tasks.Task
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl
import dev.mayankmkh.intellij.linear.models.LinearTask
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

class LinearRepository : NewBaseRepositoryImpl {

    private val apiKeyProvider = ApiKeyProvider { password }
    private val remoteDataSource = LinearRemoteDataSource(createApolloClient(URL, apiKeyProvider))

    /**
     * Serialization constructor
     */
    @Suppress("unused")
    constructor() : super()

    constructor(type: LinearRepositoryType) : super(type)

    constructor(other: LinearRepository) : super(other)

    override fun clone(): BaseRepository = LinearRepository(this)

    override fun findTask(id: String): Task? {
        TODO("Not yet implemented")
    }

    override fun getUrl(): String = URL

    override fun getPresentableName(): String = "Linear team: ${getTeamId()}"

    override fun isConfigured(): Boolean = super.isConfigured() && password.isNotBlank() && getTeamId().isNotBlank()

    private fun getTeamId() = username

    // FIXME: 05/04/21 When we add a task server, getIssues is called with null query and it is called again when task
    //  dialog is opened it already shows previously fetched issues. Now when we click 3 dots to load more issues
    //  getIssues is called again with empty query, 0 offset and limit of 20 which results in no change in list as
    //  its already showing those items. The offset should not be 0 here.
    override fun getIssues(query: String?, offset: Int, limit: Int, withClosed: Boolean): Array<LinearTask> {
        return remoteDataSource.getIssues(getTeamId(), query, offset, limit, withClosed)
    }

    override fun createCancellableConnection(): CancellableConnection {
        return object : CancellableConnection() {
            private var testJob: Job = Job()
            override fun doTest() {
                runBlocking(testJob) {
                    remoteDataSource.testConnection(getTeamId())
                }
            }

            override fun cancel() {
                testJob.cancel()
            }
        }
    }

    companion object {
        private const val URL = "https://api.linear.app/graphql"
    }
}
