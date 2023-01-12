package com.mamiksik.parrot.autocompletion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
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
import com.intellij.patterns.StandardPatterns
import com.intellij.project.stateStore
import com.intellij.psi.PsiDocumentManager
import com.mamiksik.parrot.config.PluginSettingsStateComponent
import fuel.Fuel
import fuel.HttpResponse
import fuel.post
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString


internal class CommitMessageCompletionContributor: CompletionContributor() {
    companion object {
        private val notificationManager = NotificationGroupManager.getInstance().getNotificationGroup("mamiksik.parrot.notification");
        private val icon = IconLoader.findIcon("/icons/autocomplet.svg")!!
        private val json = Json { ignoreUnknownKeys = true }
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        val project = file.project
        val document = PsiDocumentManager.getInstance(project).getDocument(file)

        if (document?.getUserData(CommitMessage.DATA_KEY) == null) return


        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(1)) // Restart when message is empty
        result.restartCompletionOnPrefixChange(StandardPatterns.string().endsWith(" ")) // Restart when message ends with space
        result.restartCompletionWhenNothingMatches() // Restart when nothing matches

        // Prevent UI freeze
        runWithCheckCanceled({
            fill(project, document.text, parameters.offset, result)
        }, ProgressManager.getInstance().progressIndicator)
    }

    private fun fill(project: Project, partialCommitMessage: String, caretOffset: Int, result: CompletionResultSet) {
        val patchStrings = getPatchStringPerFile(project)
        val commitMessage = getMaskedMessage(partialCommitMessage, caretOffset)

        val lookupElements = patchStrings
            .flatMap {
                when (caretOffset) {
                    0 -> summarize(it, "Java", project)
                    else -> fillMasked(commitMessage + it, project)
                }
            }
            .map {
                // Capitalize first letter if the commit message is empty
                val prediction = if(partialCommitMessage.isEmpty()) {
                    it.prediction.replaceFirstChar { x -> x.uppercaseChar() }
                } else {it.prediction}

                val element = LookupElementBuilder
                    .create(prediction.trim())
                    .withIcon(icon)
                    .withCaseSensitivity(false)
                    .withTypeText("""${"%.2f".format(it.score * 100)}%""")
                    .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)

                PrioritizedLookupElement.withPriority(element, it.score)
            }

        result.addAllElements(lookupElements)
    }

    private fun getMaskedMessage(partialCommitMessage: String, caretOffset: Int): String {
        return "<msg> ${partialCommitMessage.substring(0, caretOffset)}<mask>${partialCommitMessage.substring(caretOffset)}"
    }

    private fun getPatchStringPerFile(project: Project): List<String> {
        val changeListManager = ChangeListManager.getInstance(project)
        val changes = changeListManager.allChanges.map {
            val changeList = changeListManager.getChangeList(it)!!
            ChangeListChange(it, changeList.name, changeList.id)
        }

        val basePath = project.stateStore.projectBasePath
        val patches = IdeaTextPatchBuilder.buildPatch(project, changes, basePath, false, true)
            .filterIsInstance<TextFilePatch>()

        return patches.map {
            it.hunks.joinToString { hunk ->
                hunk.lines.joinToString("\n") { line ->
                    when (line.type) {
                        PatchLine.Type.CONTEXT -> "<ide> ${line.text}"
                        PatchLine.Type.ADD -> "<add> ${line.text}"
                        PatchLine.Type.REMOVE -> "<del> ${line.text}"
                    }
                }
            }
        }
    }

    private fun fillMasked(patch: String, project: Project): List<Prediction> {
        val endpoint = PluginSettingsStateComponent.instance.state.fillTokenEndpoint
        val payload = mapOf("inputs" to patch)

        val response = post(endpoint, payload, project)?: return emptyList()
        return json.decodeFromString(response.body)
    }

    private fun summarize(patch: String, language: String, project: Project): List<Prediction>{
        val endpoint = PluginSettingsStateComponent.instance.state.summarizeEndpoint
        val payload = mapOf("inputs" to patch, "lang" to language)

        val response = post(endpoint, payload, project)?: return emptyList()
        return json.decodeFromString(response.body)
    }

    private fun post(to: String, payload: Map<String, String>, project: Project): HttpResponse? {
        val token = PluginSettingsStateComponent.instance.state.inferenceApiToken
        val headers = mapOf("Authorization" to "Bearer $token")

        val response = runBlocking {
            try {
                return@runBlocking Fuel.post(url = to, body = json.encodeToString(payload), headers = headers)
            } catch (e: Exception) {
                createErrorNotification(e.message ?: "", project)
                return@runBlocking null
            }
        }

        if (response?.statusCode != 200) {
            createErrorNotification("[${response?.statusCode}]\n" + response?.body, project)
        }
        return response
    }

    private fun createErrorNotification(message: String, project: Project) {
        notificationManager.createNotification(
            "Error while sending request to Parrot API", message , NotificationType.ERROR
        ).notify(project)
    }
}
