package dev.mayankmkh.intellij.linear

import com.intellij.openapi.project.Project
import com.intellij.tasks.config.BaseRepositoryEditor
import com.intellij.util.Consumer

class LinearRepositoryEditor(
    project: Project,
    linearRepository: LinearRepository,
    changeListener: Consumer<in LinearRepository>,
) : BaseRepositoryEditor<LinearRepository>(project, linearRepository, changeListener) {
    init {
        myPasswordLabel.text = "API key:"
        myUsernameLabel.text = "Team ID:"

        myUrlLabel.isVisible = false
        myURLText.isVisible = false
        myShareUrlCheckBox.isVisible = false

        updateTestButton()

        val testUpdateListener =
            SimpleDocumentListener { e ->
                myRepository.password = String(myPasswordText.password)
                myRepository.username = myUserNameText.text
                updateTestButton()
            }
        myUserNameText.document.addDocumentListener(testUpdateListener)
        myPasswordText.document.addDocumentListener(testUpdateListener)
    }

    private fun updateTestButton() {
        myTestButton.isEnabled = myRepository.isConfigured
    }
}
