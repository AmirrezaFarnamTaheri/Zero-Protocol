package com.ghostbattery.service

import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GhostAccessibilityServiceTest {

    private lateinit var service: GhostAccessibilityService

    @Before
    fun setup() {
        service = GhostAccessibilityService()
    }

    @Test
    fun findNodesByContentDescription_findsMatchingNodes() {
        val root = AccessibilityNodeInfo.obtain()
        root.contentDescription = "Root Node"

        val child1 = AccessibilityNodeInfo.obtain()
        child1.contentDescription = "Target Button"

        val child2 = AccessibilityNodeInfo.obtain()
        child2.contentDescription = "Other Button"

        val child3 = AccessibilityNodeInfo.obtain()
        child3.contentDescription = "Another Target"

        val shadowRoot = Shadows.shadowOf(root)
        shadowRoot.addChild(child1)
        shadowRoot.addChild(child2)

        val shadowChild2 = Shadows.shadowOf(child2)
        shadowChild2.addChild(child3)

        val result = service.findNodesByContentDescription(root, "Target")

        assertEquals(2, result.size)
    }
}
