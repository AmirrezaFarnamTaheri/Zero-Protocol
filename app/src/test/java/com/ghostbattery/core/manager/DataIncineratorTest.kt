package com.ghostbattery.core.manager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DataIncineratorTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun nukeDirectory_deletesAllFilesAndFolders() {
        val root = tempFolder.newFolder("root")
        val subDir = File(root, "subdir").apply { mkdir() }
        val file1 = File(root, "file1.txt").apply { writeText("Hello") }
        val file2 = File(subDir, "file2.txt").apply { writeText("World") }

        assertTrue(file1.exists())
        assertTrue(file2.exists())

        DataIncinerator.nukeDirectory(root)

        assertFalse("File1 should be deleted", file1.exists())
        assertFalse("File2 should be deleted", file2.exists())
        assertFalse("SubDir should be deleted", subDir.exists())
        // Note: nukeDirectory calls delete() on subdirectories, but might not delete root itself if logic is subtle.
        // Let's check implementation: dir.walkBottomUp().forEach { ... if (file.isDirectory) file.delete() }
        // walkBottomUp includes the root itself. So it should delete root.
        assertFalse("Root should be deleted", root.exists())
    }
}
