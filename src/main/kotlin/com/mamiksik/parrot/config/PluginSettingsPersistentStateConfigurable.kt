package com.mamiksik.parrot.config

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import com.intellij.ui.dsl.builder.*

class PluginSettingsPersistentStateConfigurable: Configurable, Configurable.NoScroll, Disposable {
    private val configState
        get() = PluginSettingsStateComponent.instance.state

//    private val state = PluginState()
    private var inferenceApiUrlField: JTextField?= JTextField().also {
        it.text = configState.inferenceApiUrl
    }
    private var inferenceApiTokenField: JTextField? = JTextField().also {
        it.text = configState.inferenceApiToken
    }

    override fun getDisplayName(): String = "Parrot Plugin Configuration"

    override fun createComponent(): JComponent {
        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Inference Api URL", JPanel(FlowLayout(FlowLayout.LEFT)).also { it.add(inferenceApiUrlField) })
            .addLabeledComponent("Inference Api Token", JPanel(FlowLayout(FlowLayout.LEFT)).also { it.add(inferenceApiTokenField) })
            .panel

        return JPanel(BorderLayout()).also { it.add(formPanel, BorderLayout.NORTH) }
    }

    override fun dispose() {
        inferenceApiUrlField = null
        inferenceApiTokenField = null
    }

    override fun isModified(): Boolean {

        return configState.inferenceApiToken != inferenceApiTokenField!!.text
                || configState.inferenceApiUrl != inferenceApiUrlField!!.text
    }

    override fun apply() {
        configState.inferenceApiUrl = inferenceApiUrlField!!.text
        configState.inferenceApiToken = inferenceApiTokenField!!.text
    }


    override fun reset() {
        inferenceApiUrlField!!.text = configState.inferenceApiUrl
        inferenceApiTokenField!!.text = configState.inferenceApiToken
    }
}