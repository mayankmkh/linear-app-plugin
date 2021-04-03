package dev.mayankmkh.intellij.linear.services

import com.intellij.openapi.project.Project
import dev.mayankmkh.intellij.linear.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
