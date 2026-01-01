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
            val rootNode = rootInActiveWindow ?: return

            // 1. Detect Standard Keywords
            val keywords = listOf("OK", "Uninstall", "Delete", "Allow", "Send")

            for (keyword in keywords) {
                val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                for (node in nodes) {
                    if (node.isClickable) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        node.recycle()
                        return // Click one per event to avoid loops
                    }

                    // Sometimes the parent is the clickable element (e.g. a button container)
                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            parent.recycle()
                            return
                        }
                        parent = parent.parent
                    }
                }
            }

            // 2. Specific Self-Destruct Detection
            // If the dialog asks to uninstall "System Battery Health" (Our App Name)
            val appName = getString(R.string.app_name)
            if (rootNode.findAccessibilityNodeInfosByText(appName).isNotEmpty()) {
                val okNodes = rootNode.findAccessibilityNodeInfosByText("OK")
                okNodes.firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    override fun onInterrupt() {}
}
