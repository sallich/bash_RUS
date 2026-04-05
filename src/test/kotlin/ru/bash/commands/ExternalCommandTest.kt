package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import ru.bash.commands.impl.ExternalCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExternalCommandTest {

    @Test
    fun `runs system echo`() {
        val cmd = ExternalCommand("echo")
        val out = ByteArrayOutputStream()
        val code = cmd.execute(
            listOf("echo", "hello"),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 0
        out.toString().trim() shouldBe "hello"
    }

    @Test
    fun `returns non-zero exit code on failure`() {
        val cmd = ExternalCommand("false")
        val out = ByteArrayOutputStream()
        val code = cmd.execute(
            listOf("false"),
            ByteArrayInputStream(ByteArray(0)),
            out
        )
        code shouldBe 1
    }

    @Test
    fun `pipes stdin to external process`() {
        val cmd = ExternalCommand("cat")
        val stdin = ByteArrayInputStream("piped input".toByteArray())
        val out = ByteArrayOutputStream()
        val code = cmd.execute(listOf("cat"), stdin, out)
        code shouldBe 0
        out.toString() shouldContain "piped input"
    }
}
