package dev.mayankmkh.intellij.linear

import com.intellij.tasks.Task
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl

class LinearRepository : NewBaseRepositoryImpl {

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
}
