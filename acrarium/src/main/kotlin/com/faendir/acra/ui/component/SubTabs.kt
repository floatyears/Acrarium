/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.component

import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.Synchronize
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.Tabs.SelectedChangeEvent
import com.vaadin.flow.shared.Registration

/**
 * @author lukas
 * @since 06.09.19
 */
open class SubTabs(vararg tabs: Tab?) : Tab(), HasComponents {
    private val content: Tabs = Tabs(false, *tabs)
    private var registration: Registration? = null

    init {
        content.orientation = Tabs.Orientation.VERTICAL
        content.addSelectedChangeListener { e: SelectedChangeEvent ->
            parent.ifPresent { parent: Component? ->
                if (e.selectedTab != null && parent is Tabs) {
                    parent.selectedIndex = -1
                }
            }
        }
        content.setWidthFull()
        super<Tab>.add(content)
        hideIfEmpty()
    }

    override fun onAttach(attachEvent: AttachEvent) {
        parent.ifPresent { parent: Component? ->
            if (parent is Tabs) {
                registration = parent.addSelectedChangeListener { e: SelectedChangeEvent ->
                    if (isVisible) {
                        if (e.selectedTab === this) {
                            content.selectedIndex = 0
                        } else if (e.selectedTab != null) {
                            content.selectedIndex = -1
                        }
                    }
                }
            }
        }
    }

    override fun onDetach(detachEvent: DetachEvent) {
        if (registration != null) {
            registration!!.remove()
        }
    }

    override fun add(vararg components: Component) {
        content.add(*components)
        hideIfEmpty()
    }

    override fun remove(vararg components: Component) {
        content.remove(*components)
        hideIfEmpty()
    }

    override fun removeAll() {
        content.removeAll()
        hideIfEmpty()
    }

    override fun addComponentAtIndex(index: Int, component: Component) {
        content.addComponentAtIndex(index, component)
        hideIfEmpty()
    }

    fun replace(oldComponent: Component?, newComponent: Component?) {
        content.replace(oldComponent, newComponent)
        hideIfEmpty()
    }

    fun addSelectedChangeListener(listener: (SelectedChangeEvent) -> Unit): Registration {
        return content.addSelectedChangeListener(listener)
    }

    @get:Synchronize(property = "selected", value = ["selected-changed"])
    var selectedIndex: Int
        get() = content.selectedIndex
        set(selectedIndex) {
            content.selectedIndex = selectedIndex
        }

    var selectedTab: Tab?
        get() = content.selectedTab
        set(selectedTab) {
            content.selectedTab = selectedTab
        }

    private fun hideIfEmpty() {
        isVisible = content.componentCount > 0
    }
}