package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.bash.commands.impl.GrepCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.writeText

class GrepCommandTest {
    private val grep = GrepCommand()

    @Test
    fun `grep search from stdin with pattern`() {
        val stdin = ByteArrayInputStream("grep me please".toByteArray())
        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", ""),
            stdin,
            out
        )
        code shouldBe 0
        out.toString() shouldBe "grep me please\n"
    }

    @Test
    fun `grep search from stdin with pattern and -c option`() {
        val stdin = ByteArrayInputStream("aaa\nbbb\nccc".toByteArray())
        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "-c", "a"),
            stdin,
            out
        )
        code shouldBe 0
        out.toString() shouldBe "1\n"
    }

    @Test
    fun `grep search from stdin with pattern and -c,-i options`() {
        val stdin = ByteArrayInputStream("aaa\nbbb\nccc".toByteArray())
        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "-c", "-i", "A"),
            stdin,
            out
        )
        code shouldBe 0
        out.toString() shouldBe "1\n"
    }

    @Test
    fun `grep search from file with empty pattern`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt").also { it.writeText("file content") }

        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "", file.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString() shouldBe "file content\n"
    }

    @Test
    fun `grep search from file with one letter pattern and -i option`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt").also { it.writeText("TEST") }

        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "-i", "e", file.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString() shouldBe "TEST\n"
    }

    @Test
    fun `grep search from multiline file with -A option`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt").also { it.writeText("TEST\ncontext1\ncontext2") }

        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "-A", "2", "TEST", file.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString() shouldBe "TEST\n" +
                "context1\n" +
                "context2\n"
    }

    @Test
    fun `grep search from multiline file`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt").also { it.writeText("TEST\ncontext1\ncontext2") }

        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "TEST", file.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString() shouldBe "TEST\n"
    }

    @Test
    fun `grep search from multiple files`(@TempDir dir: Path) {
        val file1 = dir.resolve("1.txt").also { it.writeText("file1 content") }
        val file2 = dir.resolve("2.txt").also { it.writeText("file2 content") }

        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "file", file1.toString(), file2.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString() shouldBe "$file1:file1 content\n$file2:file2 content\n"
    }

    @Test
    fun `grep search from multiple files and option -w`(@TempDir dir: Path) {
        val file1 = dir.resolve("1.txt").also { it.writeText("file1 content") }
        val file2 = dir.resolve("2.txt").also { it.writeText("file2 content") }

        val out = ByteArrayOutputStream()
        val code1 = grep.execute(
            listOf("grep", "-w", "file", file1.toString(), file2.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code1 shouldBe 1
        out.toString() shouldBe ""

        val code2 = grep.execute(
            listOf("grep", "-w", "content", file1.toString(), file2.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )

        code2 shouldBe 0
        out.toString() shouldBe "$file1:file1 content\n" +
                "$file2:file2 content\n"

    }

    @Test
    fun `grep search from multiple files and option -l`(@TempDir dir: Path) {
        val file1 = dir.resolve("1.txt").also { it.writeText("file1 content") }
        val file2 = dir.resolve("2.txt").also { it.writeText("file2 content") }

        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "-l", "file", file1.toString(), file2.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString() shouldBe "$file1\n$file2\n"
    }

    @Test
    fun `grep search from not exist file`(@TempDir dir: Path) {
        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "", "/nonexistent/file.txt"),
            ByteArrayInputStream(ByteArray(0)),
            out
        )

        code shouldBe 2
        out.toString() shouldContain "No such file or directory"
    }

    @Test
    fun `grep search from stdin with no matching`() {
        val stdin = ByteArrayInputStream("grep me please".toByteArray())
        val out = ByteArrayOutputStream()
        val code = grep.execute(
            listOf("grep", "abc"),
            stdin,
            out
        )
        code shouldBe 1
        out.toString() shouldBe ""
    }
}