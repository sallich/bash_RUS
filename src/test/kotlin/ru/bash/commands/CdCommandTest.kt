package ru.bash.commands

import org.junit.jupiter.api.Test
import ru.bash.commands.impl.CdCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class CdCommandTest {
    private val cd = CdCommand()

    @Test
    fun `cd to the home directory`() {
        val code = cd.execute(
            listOf("cd"),
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            ByteArrayOutputStream()
        )
        val homeDir = System.getProperty("user.home")
        val curDir = System.getProperty("user.dir")
        code shouldBe 0
        curDir shouldBe homeDir
    }

    @Test
    fun `cd to the temp directory`(@TempDir dir: Path) {
        val code = cd.execute(
            listOf("cd", dir.toAbsolutePath().toString()),
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            ByteArrayOutputStream()
        )
        val curDir = System.getProperty("user.dir")
        println(dir.toString())
        code shouldBe 0
        curDir shouldBe dir.toAbsolutePath().toString()
    }

    @Test
    fun `cd to the temp directory with different path`(@TempDir dir: Path) {
        val code = cd.execute(
            listOf("cd", dir.toAbsolutePath().toString() + File.separator + ".." + File.separator + dir.fileName),
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            ByteArrayOutputStream()
        )
        val curDir = System.getProperty("user.dir")
        println(dir.toString())
        code shouldBe 0
        curDir shouldBe dir.toAbsolutePath().toString()
    }

    @Test
    fun `cd to the file`(@TempDir dir: Path) {
        val origDir = System.getProperty("user.dir")
        val file = dir.resolve("1.txt").also { it.writeText("file1 content") }
        val err = ByteArrayOutputStream()
        val code = cd.execute(
            listOf("cd", file.toAbsolutePath().toString()),
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            err
        )
        err.toString() shouldContain "Not a directory"
        origDir shouldBe System.getProperty("user.dir")
        code shouldBe 1
    }

    @Test
    fun `cd to the non existent directory`(@TempDir dir: Path) {
        Files.deleteIfExists(dir)
        val err = ByteArrayOutputStream()
        val code = cd.execute(
            listOf("cd", dir.toAbsolutePath().toString()),
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            err
        )
        err.toString() shouldContain "No such file or directory"
        code shouldBe 1
    }

}
