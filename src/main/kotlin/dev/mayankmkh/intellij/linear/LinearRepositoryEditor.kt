package dev.mayankmkh.intellij.linear

import com.intellij.openapi.project.Project
import com.intellij.tasks.config.BaseRepositoryEditor
import com.intellij.ui.components.JBLabel
import com.intellij.util.Consumer
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JTextField

class LinearRepositoryEditor(
    project: Project,
    linearRepository: LinearRepository,
    changeListener: Consumer<in LinearRepository>,
) : BaseRepositoryEditor<LinearRepository>(project, linearRepository, changeListener) {
    private lateinit var myWorkspaceLabel: JBLabel
    private lateinit var myWorkspaceText: JTextField

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

    override fun createCustomPanel(): JComponent? {
        myWorkspaceLabel = JBLabel("Workspace ID:")
        myWorkspaceText = JTextField(myRepository.workspaceId)
        installListener(myWorkspaceText)
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(myWorkspaceLabel, myWorkspaceText)
            .panel
    }

    override fun apply() {
        myRepository.workspaceId = myWorkspaceText.text.trim()
        super.apply()
        updateTestButton()
    }

    private fun updateTestButton() {
        myTestButton.isEnabled = myRepository.isConfigured
    }
}
