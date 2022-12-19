package com.example.mamiksik.intelijplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.Refreshable

class GenerateCommitMessageAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val commitPanel = getCommitPanel(e) ?: return
        commitPanel.setCommitMessage("Test")
    }

    private fun getCommitPanel(e: AnActionEvent): CommitMessageI? {
        val data = Refreshable.PANEL_KEY.getData(e.dataContext) as? CommitMessageI
        if (data is CommitMessageI) return data

        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext)
    }
}