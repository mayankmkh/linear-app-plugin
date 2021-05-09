package dev.mayankmkh.intellij.linear.models

import apolloGenerated.dev.mayankmkh.intellij.linear.IssuesQuery
import com.intellij.tasks.Comment
import java.util.Date

class LinearComment(private val node: IssuesQuery.Node2) : Comment() {
    override fun getText(): String = node.body

    override fun getAuthor(): String = node.user.name

    override fun getDate(): Date = node.createdAt
}
