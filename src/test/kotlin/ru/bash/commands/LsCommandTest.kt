package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.bash.commands.impl.LsCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText

class LsCommandTest {

    private val ls = LsCommand()
    private val emptyStdin = ByteArrayInputStream(ByteArray(0))

    private fun run(vararg argv: String): Pair<String, String> {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        ls.execute(listOf("ls", *argv), emptyStdin, out, err)
        return out.toString() to err.toString()
    }

    private fun exitCode(vararg argv: String, err: ByteArrayOutputStream = ByteArrayOutputStream()): Int {
        val out = ByteArrayOutputStream()
        return ls.execute(listOf("ls", *argv), emptyStdin, out, err)
    }

    @Test
    fun `ls lists directory contents sorted`(@TempDir dir: Path) {
        dir.resolve("b.txt").writeText("")
        dir.resolve("a.txt").writeText("")
        dir.resolve("c.txt").writeText("")

        val (out, _) = run(dir.toString())
        out shouldBe "a.txt\nb.txt\nc.txt\n"
    }

    @Test
    fun `ls lists subdirectories`(@TempDir dir: Path) {
        dir.resolve("file.txt").writeText("")
        dir.resolve("subdir").createDirectory()

        val (out, _) = run(dir.toString())
        out shouldBe "file.txt\nsubdir\n"
    }

    @Test
    fun `ls hides dotfiles by default`(@TempDir dir: Path) {
        dir.resolve(".hidden").writeText("")
        dir.resolve("visible.txt").writeText("")

        val (out, _) = run(dir.toString())
        out shouldBe "visible.txt\n"
    }

    @Test
    fun `ls on empty directory produces empty output`(@TempDir dir: Path) {
        val (out, err) = run(dir.toString())
        out shouldBe ""
        err shouldBe ""
    }

    @Test
    fun `ls on a file prints the file path`(@TempDir dir: Path) {
        val file = dir.resolve("one.txt").also { it.writeText("x") }

        val (out, _) = run(file.toString())
        out shouldBe "${file}\n"
    }

    @Test
    fun `ls returns 1 and writes to stderr for missing path`() {
        val err = ByteArrayOutputStream()
        val code = exitCode("/nonexistent/path", err = err)
        code shouldBe 1
        err.toString() shouldContain "No such file or directory"
    }

    @Test
    fun `ls with multiple paths prints headers`(@TempDir dir: Path) {
        val d1 = dir.resolve("d1").also { it.createDirectory() }
        val d2 = dir.resolve("d2").also { it.createDirectory() }
        d1.resolve("x").writeText("")
        d2.resolve("y").writeText("")

        val (out, _) = run(d1.toString(), d2.toString())
        out shouldBe "$d1:\nx\n\n$d2:\ny\n"
    }

    @Test
    fun `ls with no args lists current working directory`(@TempDir dir: Path) {
        dir.resolve("only.txt").writeText("")
        val original = System.getProperty("user.dir")
        System.setProperty("user.dir", dir.toString())
        try {
            val (out, _) = run()
            out shouldBe "only.txt\n"
        } finally {
            System.setProperty("user.dir", original)
        }
    }

    @Test
    fun `ls returns 1 and writes to stderr for directory without read permission`(@TempDir dir: Path) {
        val unreadable = dir.resolve("locked").also { it.createDirectory() }
        val file = unreadable.toFile()
        check(file.setReadable(false, false)) { "cannot drop read permission on $unreadable" }
        try {
            val out = ByteArrayOutputStream()
            val err = ByteArrayOutputStream()
            val code = ls.execute(listOf("ls", unreadable.toString()), emptyStdin, out, err)
            code shouldBe 1
            out.toString() shouldBe ""
            err.toString() shouldContain "Permission denied"
        } finally {
            file.setReadable(true, true)
        }
    }

    @Test
    fun `ls continues after a missing path`(@TempDir dir: Path) {
        dir.resolve("ok.txt").writeText("")

        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val code = ls.execute(
            listOf("ls", "/nonexistent", dir.toString()),
            emptyStdin, out, err
        )
        code shouldBe 1
        out.toString() shouldContain "ok.txt"
        err.toString() shouldContain "No such file or directory"
    }
}
