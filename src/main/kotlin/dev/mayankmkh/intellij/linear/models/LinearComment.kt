package dev.mayankmkh.intellij.linear.models

import com.intellij.tasks.Comment
import dev.mayankmkh.intellij.linear.IssuesQuery
import java.util.Date

class LinearComment(private val node: IssuesQuery.Node2) : Comment() {
    override fun getText(): String = node.body

    override fun getAuthor(): String = node.user.name

    override fun getDate(): Date = node.createdAt
}
