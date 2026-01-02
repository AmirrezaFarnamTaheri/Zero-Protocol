package com.ghostbattery.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager

class GhostAccessibilityService : AccessibilityService() {

    private lateinit var prefsManager: PrefsManager

    override fun onServiceConnected() {
        prefsManager = PrefsManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Safety: Only automate if Panic Mode is active
        if (!prefsManager.isPanicModeActive) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val rootNode = rootInActiveWindow ?: return

            try {
                // STRATEGY 1: ID-BASED CLICKING (Language Independent)
                // "button1" is the standard ID for Positive/OK/Yes/Delete buttons in Android
                val standardPositiveButtons = rootNode.findAccessibilityNodeInfosByViewId("android:id/button1")
                for (btn in standardPositiveButtons) {
                    if (btn.isClickable && btn.isEnabled) {
                        btn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        btn.recycle()
                        // If we clicked the standard button, we are likely done with this dialog
                        return
                    }
                    btn.recycle()
                }

                // STRATEGY 2: KEYWORD FALLBACK (For non-standard dialogs)
                val keywords = listOf(
                    "OK", "Uninstall", "Delete", "Allow", "Send", "Remove", "Erase", "Yes",
                    "Desinstalar", "Eliminar", "Permitir", "Enviar" // Common Spanish fallback
                )

                for (keyword in keywords) {
                    val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                    for (node in nodes) {
                        // Attempt to click node or its parent
                        if (performClick(node)) {
                            node.recycle()
                            return
                        }
                        node.recycle()
                    }
                }

                // STRATEGY 3: SELF-DESTRUCT SPECIFIC
                // Detect our own uninstall dialog specifically
                val appName = getString(R.string.app_name)
                if (rootNode.findAccessibilityNodeInfosByText(appName).isNotEmpty()) {
                    // Aggressively look for ANY clickable button if we see our app name
                    clickAnyButton(rootNode)
                }

            } catch (e: Exception) {
                // Fail silently, don't crash the service
            } finally {
                // Do not recycle rootNode here as it can cause issues in some Android versions
                // or let the system handle it
            }
        }
    }

    private fun performClick(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable && node.isEnabled) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        // Try parent (often text is inside a clickable container)
        val parent = node.parent
        if (parent != null) {
            val res = performClick(parent)
            parent.recycle()
            return res
        }
        return false
    }

    private fun clickAnyButton(node: AccessibilityNodeInfo): Boolean {
        if (node.className == "android.widget.Button" && node.isClickable && node.isEnabled) {
             return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (clickAnyButton(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }
        return false
    }

    override fun onInterrupt() {}
}
