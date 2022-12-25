package com.mamiksik.parrot.config
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "PluginSettingsComponent",
    storages = [Storage("parrot-plugin-settings.xml")]
)
open class PluginSettingsStateComponent: PersistentStateComponent<PluginSettingsState> {

    companion object {
        val instance: PluginSettingsStateComponent
            get() = ServiceManager.getService(PluginSettingsStateComponent::class.java)
    }

    private var state = PluginSettingsState()

    override fun getState(): PluginSettingsState {
        return state
    }

    override fun loadState(state: PluginSettingsState) {
        this.state = state
    }
}