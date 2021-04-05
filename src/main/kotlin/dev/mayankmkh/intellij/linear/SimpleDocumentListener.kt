package dev.mayankmkh.intellij.linear

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

fun interface SimpleDocumentListener : DocumentListener {
    override fun insertUpdate(e: DocumentEvent?) = onUpdate(e)

    override fun removeUpdate(e: DocumentEvent?) = onUpdate(e)

    override fun changedUpdate(e: DocumentEvent?) = onUpdate(e)

    fun onUpdate(e: DocumentEvent?)
}
