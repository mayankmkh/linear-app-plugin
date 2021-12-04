package dev.mayankmkh.intellij.linear.models

import apolloGenerated.dev.mayankmkh.intellij.linear.fragment.ShortIssueConnection
import com.intellij.tasks.Comment
import com.intellij.tasks.Task
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.TaskType
import com.intellij.util.containers.map2Array
import dev.mayankmkh.intellij.linear.LinearRepository
import icons.LinearPluginIcons
import java.util.Date
import javax.swing.Icon

@SuppressWarnings("TooManyFunctions")
class LinearTask(private val node: ShortIssueConnection.Node, private val repository: LinearRepository) : Task() {

    override fun getId(): String = node.identifier

    override fun getSummary(): String = node.title

    override fun getDescription(): String? = node.description

    override fun getComments(): Array<Comment> {
        return node.comments.nodes.map2Array { LinearComment(it) }
    }

    override fun getIcon(): Icon = LinearPluginIcons.Logo

    override fun getType(): TaskType {
        node.labels.nodes.forEach {
            val taskType = when (it.name) {
                "Feature" -> TaskType.FEATURE
                "Bug" -> TaskType.BUG
                "Improvement" -> TaskType.FEATURE
                else -> null
            }
            if (taskType != null) return taskType
        }
        return TaskType.OTHER
    }

    override fun getUpdated(): Date = node.updatedAt

    override fun getCreated(): Date = node.createdAt

    override fun isClosed(): Boolean = node.state.type == "completed" || node.state.type == "canceled"

    override fun isIssue(): Boolean = true

    override fun getIssueUrl(): String = node.url

    override fun getRepository(): TaskRepository = repository
}
