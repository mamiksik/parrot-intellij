package com.mamiksik.parrot.config

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class PluginSettingsPersistentStateConfigurable: Configurable, Configurable.NoScroll, Disposable {
    private val configState
        get() = PluginSettingsStateComponent.instance.state


    private var fillTokenEndpointField: JTextField?= JTextField().also {
        it.text = configState.fillTokenEndpoint
    }

    private var summarizeEndpointField: JTextField?= JTextField().also {
        it.text = configState.summarizeEndpoint
    }

    private var inferenceApiTokenField: JTextField? = JTextField().also {
        it.text = configState.inferenceApiToken
    }

    override fun getDisplayName(): String = "Parrot Plugin Configuration"

    override fun createComponent(): JComponent {
        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Inference api URL", JPanel(FlowLayout(FlowLayout.LEFT)).also { it.add(fillTokenEndpointField) })
            .addLabeledComponent("Fill Token api token", JPanel(FlowLayout(FlowLayout.LEFT)).also { it.add(inferenceApiTokenField) })
            .addLabeledComponent("Summarize api token", JPanel(FlowLayout(FlowLayout.LEFT)).also { it.add(summarizeEndpointField) })
            .panel

        return JPanel(BorderLayout()).also { it.add(formPanel, BorderLayout.NORTH) }
    }

    override fun dispose() {
        fillTokenEndpointField = null
        inferenceApiTokenField = null
    }

    override fun isModified(): Boolean {

        return configState.inferenceApiToken != inferenceApiTokenField!!.text
                || configState.fillTokenEndpoint != fillTokenEndpointField!!.text
                || configState.summarizeEndpoint != summarizeEndpointField!!.text
    }

    override fun apply() {
        configState.fillTokenEndpoint = fillTokenEndpointField!!.text
        configState.summarizeEndpoint = summarizeEndpointField!!.text
        configState.inferenceApiToken = inferenceApiTokenField!!.text
    }


    override fun reset() {
        fillTokenEndpointField!!.text = configState.fillTokenEndpoint
        summarizeEndpointField!!.text = configState.summarizeEndpoint
        inferenceApiTokenField!!.text = configState.inferenceApiToken
    }
}