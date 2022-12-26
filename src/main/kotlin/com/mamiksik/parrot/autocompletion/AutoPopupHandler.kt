package com.mamiksik.parrot.autocompletion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile


internal class AutoPopupHandler: TypedHandlerDelegate() {
    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if ((editor as? EditorImpl)?.placeholder?.equals("Commit Message") == true) {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }
}
