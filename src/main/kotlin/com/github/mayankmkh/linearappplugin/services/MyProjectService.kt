package com.github.mayankmkh.linearappplugin.services

import com.github.mayankmkh.linearappplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
