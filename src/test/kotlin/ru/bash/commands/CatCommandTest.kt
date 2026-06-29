package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.bash.commands.impl.CatCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.writeText

class CatCommandTest {

    private val cat = CatCommand()

    @Test
    fun `cat reads from stdin when no files given`() {
        val stdin = ByteArrayInputStream("hello from stdin".toByteArray())
        val out = ByteArrayOutputStream()
        val code = cat.execute(listOf("cat"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString() shouldBe "hello from stdin"
    }

    @Test
    fun `cat reads file contents`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt")
        file.writeText("file content")

        val out = ByteArrayOutputStream()
        val code = cat.execute(
            listOf("cat", file.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out,
            ByteArrayOutputStream()
        )
        code shouldBe 0
        out.toString() shouldBe "file content"
    }

    @Test
    fun `cat concatenates multiple files`(@TempDir dir: Path) {
        val a = dir.resolve("a.txt").also { it.writeText("AAA") }
        val b = dir.resolve("b.txt").also { it.writeText("BBB") }

        val out = ByteArrayOutputStream()
        val code = cat.execute(
            listOf("cat", a.toString(), b.toString()),
            ByteArrayInputStream(ByteArray(0)),
            out,
            ByteArrayOutputStream()
        )
        code shouldBe 0
        out.toString() shouldBe "AAABBB"
    }

    @Test
    fun `cat returns 1 for missing file`() {
        val err = ByteArrayOutputStream()
        val code = cat.execute(
            listOf("cat", "/nonexistent/file.txt"),
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream(),
            err
        )
        code shouldBe 1
        err.toString() shouldContain "No such file or directory"
    }
}
