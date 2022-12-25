package com.mamiksik.parrot.config
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "PluginSettingsComponent",
    storages = [Storage("parrot-plugin-settings.xml")]
)
open class PluginSettingsStateComponent: PersistentStateComponent<PluginState> {

    companion object {
        val instance: PluginSettingsStateComponent
            get() = ServiceManager.getService(PluginSettingsStateComponent::class.java)
    }

    private var state = PluginState()

    override fun getState(): PluginState {
        return state
    }

    override fun loadState(state: PluginState) {
        this.state = state
    }
}