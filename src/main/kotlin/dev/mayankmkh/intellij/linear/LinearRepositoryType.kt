package dev.mayankmkh.intellij.linear

import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.config.TaskRepositoryEditor
import com.intellij.tasks.impl.BaseRepositoryType
import com.intellij.util.Consumer
import icons.LinearPluginIcons
import javax.swing.Icon

class LinearRepositoryType : BaseRepositoryType<LinearRepository>() {
    override fun getName(): String = "Linear"

    override fun getIcon(): Icon = LinearPluginIcons.Logo

    override fun createRepository(): TaskRepository = LinearRepository(this)

    override fun getRepositoryClass(): Class<LinearRepository> = LinearRepository::class.java

    override fun createEditor(
        repository: LinearRepository,
        project: Project,
        changeListener: Consumer<in LinearRepository>,
    ): TaskRepositoryEditor {
        return LinearRepositoryEditor(project, repository, changeListener)
    }
}
