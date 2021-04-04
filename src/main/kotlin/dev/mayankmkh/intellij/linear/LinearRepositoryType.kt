package dev.mayankmkh.intellij.linear

import com.intellij.tasks.TaskRepository
import com.intellij.tasks.impl.BaseRepositoryType
import icons.LinearPluginIcons
import javax.swing.Icon

class LinearRepositoryType : BaseRepositoryType<LinearRepository>() {
    override fun getName(): String = "Linear"

    override fun getIcon(): Icon = LinearPluginIcons.Logo

    override fun createRepository(): TaskRepository = LinearRepository(this)

    override fun getRepositoryClass(): Class<LinearRepository> = LinearRepository::class.java
}
