package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.bash.commands.impl.WcCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.writeText

class WcCommandTest {

    private val wc = WcCommand()
    private val emptyStdin = ByteArrayInputStream(ByteArray(0))

    @Test
    fun `wc counts lines words and bytes from stdin`() {
        val stdin = ByteArrayInputStream("hello world\n".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "1       2      12"
    }

    @Test
    fun `wc counts multiline stdin`() {
        val stdin = ByteArrayInputStream("line one\nline two\nline three\n".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "3       6      29"
    }

    @Test
    fun `wc -l counts only lines from stdin`() {
        val stdin = ByteArrayInputStream("aaa\nbbb\nccc\n".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc", "-l"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "3"
    }

    @Test
    fun `wc -w counts only words from stdin`() {
        val stdin = ByteArrayInputStream("one two three\n".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc", "-w"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "3"
    }

    @Test
    fun `wc -c counts only bytes from stdin`() {
        val stdin = ByteArrayInputStream("hello\n".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc", "-c"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "6"
    }

    @Test
    fun `wc -lw counts lines and words`() {
        val stdin = ByteArrayInputStream("one two\nthree\n".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc", "-lw"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "2       3"
    }

    @Test
    fun `wc reads from file`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt").also { it.writeText("hello world\n") }
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc", file.toString()), emptyStdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "1       2      12 $file"
    }

    @Test
    fun `wc -l reads from file`(@TempDir dir: Path) {
        val file = dir.resolve("test.txt").also { it.writeText("aaa\nbbb\n") }
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc", "-l", file.toString()), emptyStdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "2 $file"
    }

    @Test
    fun `wc counts multiple files with total`(@TempDir dir: Path) {
        val f1 = dir.resolve("a.txt").also { it.writeText("one\n") }
        val f2 = dir.resolve("b.txt").also { it.writeText("two three\n") }
        val out = ByteArrayOutputStream()
        val code = wc.execute(
            listOf("wc", f1.toString(), f2.toString()),
            emptyStdin, out, ByteArrayOutputStream()
        )
        code shouldBe 0
        val lines = out.toString().trimEnd().lines()
        lines.size shouldBe 3
        lines[0].trim() shouldBe "1       1       4 $f1"
        lines[1].trim() shouldBe "1       2      10 $f2"
        lines[2].trim() shouldBe "2       3      14 total"
    }

    @Test
    fun `wc returns 1 for missing file`() {
        val err = ByteArrayOutputStream()
        val out = ByteArrayOutputStream()
        val code = wc.execute(
            listOf("wc", "/nonexistent/file.txt"),
            emptyStdin, out, err
        )
        code shouldBe 1
        err.toString() shouldContain "No such file or directory"
    }

    @Test
    fun `wc empty stdin`() {
        val stdin = ByteArrayInputStream(ByteArray(0))
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "0       0       0"
    }

    @Test
    fun `wc stdin without trailing newline`() {
        val stdin = ByteArrayInputStream("hello".toByteArray())
        val out = ByteArrayOutputStream()
        val code = wc.execute(listOf("wc"), stdin, out, ByteArrayOutputStream())
        code shouldBe 0
        out.toString().trim() shouldBe "0       1       5"
    }
}
