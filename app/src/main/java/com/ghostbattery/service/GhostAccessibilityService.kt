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

            val pkg = event.packageName?.toString() ?: return
            val defaultAllowed = setOf(
                "com.android.packageinstaller",
                "com.google.android.packageinstaller",
                "com.android.permissioncontroller",
                "com.google.android.apps.messaging",
                "com.whatsapp",
                "org.telegram.messenger",
                "com.instagram.android",
                "com.twitter.android"
            )
            // Combine default hardcoded list with user-specified list from preferences
            val allowed = defaultAllowed + prefsManager.allowedAccessibilityPackages

            if (pkg !in allowed) return

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
                    // Search by Text
                    val nodesByText = rootNode.findAccessibilityNodeInfosByText(keyword)
                    for (node in nodesByText) {
                        if (performClick(node)) {
                            node.recycle()
                            return
                        }
                        node.recycle()
                    }

                    // Search by Content Description (for icon-only buttons)
                    // We traverse BFS/DFS to find nodes with matching content description since standard API doesn't support findByContentDescription directly
                    val nodesByDesc = findNodesByContentDescription(rootNode, keyword)
                    for (node in nodesByDesc) {
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
                    // Do not recycle the child node here. The view hierarchy is stale
                    // after a click action, and recycling can cause a crash.
                    return true
                }
                child.recycle()
            }
        }
        return false
    }

    private fun findNodesByContentDescription(root: AccessibilityNodeInfo, keyword: String): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        val queue = java.util.LinkedList<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: continue

            if (node.contentDescription != null && node.contentDescription.toString().contains(keyword, ignoreCase = true)) {
                result.add(node)
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        return result
    }

    override fun onInterrupt() {}
}
