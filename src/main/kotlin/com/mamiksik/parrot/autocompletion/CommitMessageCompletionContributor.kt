package com.mamiksik.parrot.autocompletion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ex.ApplicationUtil.runWithCheckCanceled
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.PatchLine
import com.intellij.openapi.diff.impl.patch.TextFilePatch
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vcs.changes.ChangeListChange
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.project.stateStore
import com.intellij.psi.PsiDocumentManager
import fuel.Fuel
import fuel.post
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import io.github.cdimascio.dotenv.dotenv


internal class CommitMessageCompletionContributor: CompletionContributor() {
    companion object {
        private val dotenv = dotenv()
        private val icon = IconLoader.findIcon("/icons/autocomplet.svg")!!
    }
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        val project = file.project
        val document = PsiDocumentManager.getInstance(project).getDocument(file)

        if (document?.getUserData(CommitMessage.DATA_KEY) == null) return


//        result.restartCompletionOnAnyPrefixChange()
//        result.restartCompletionWhenNothingMatches()

        // Prevent UI freeze
        runWithCheckCanceled({
            fill(project, document.text, result)
        }, ProgressManager.getInstance().progressIndicator)
    }

    private fun fill(project: Project, partialCommitMessage: String, result: CompletionResultSet) {
        val patchStrings = getPatchStringPerFile(project)
        val commitMessage = getMaskedMessage(partialCommitMessage)

        val lookupElements = patchStrings
            .flatMap { predict(commitMessage + it) }
            .sortedBy { it.score }
            .map {
                var prediction = if(partialCommitMessage.isEmpty()) {it.prediction.capitalize()} else {it.prediction}
                prediction = prediction.trim()

                LookupElementBuilder
                    .create(prediction)
                    .withIcon(icon)
                    .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
            }

        result.addAllElements(lookupElements)
    }


    private fun getMaskedMessage(partialCommitMessage: String): String {
        return "<msg> " + when {
            partialCommitMessage.isEmpty() -> "<mask>\n"
            partialCommitMessage.endsWith(" ") -> "$partialCommitMessage <mask>\n"
            else -> "$partialCommitMessage<mask>\n"
        }
    }

    private fun getPatchStringPerFile(project: Project): List<String> {
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges.map {
            val changeList = changeListManager.getChangeList(it)!!
            ChangeListChange(it, changeList.name, changeList.id)
        }

        val basePath = project.stateStore.projectBasePath
        val patches = IdeaTextPatchBuilder.buildPatch(project, changes, basePath, false, true) as List<TextFilePatch>

        return patches.map {
            it.hunks.joinToString { hunk ->
                hunk.lines.joinToString("\n") { line ->
                    when (line.type) {
                        PatchLine.Type.CONTEXT -> "<keep> ${line.text}"
                        PatchLine.Type.ADD -> "<add> ${line.text}"
                        PatchLine.Type.REMOVE -> "<remove> ${line.text}"
                    }
                }
            }
        }
    }

    private fun predict(patch: String): List<Prediction> {
        val json = Json { ignoreUnknownKeys = true }
        val apiUrl = "https://api-inference.huggingface.co/models/mamiksik/CommitPredictor"
        val token = dotenv["HF_API_TOKEN"]

        val payload = mapOf("inputs" to patch)
        val headers = mapOf("Authorization" to "Bearer $token")


        val request = runBlocking {
            Fuel.post(url=apiUrl, body=json.encodeToString(payload), headers=headers)
        }

        if (request.statusCode != 200) {
            return emptyList()
        }

        return json.decodeFromString(request.body)
    }
}