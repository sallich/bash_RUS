package ru.bash.commands

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.bash.commands.impl.EchoCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EchoCommandTest {

    private val echo = EchoCommand()
    private val emptyStdin = ByteArrayInputStream(ByteArray(0))

    private fun run(vararg argv: String): String {
        val out = ByteArrayOutputStream()
        echo.execute(argv.toList(), emptyStdin, out)
        return out.toString()
    }

    @Test
    fun `echo with no args prints newline`() {
        run("echo") shouldBe "\n"
    }

    @Test
    fun `echo prints arguments joined by space`() {
        run("echo", "hello", "world") shouldBe "hello world\n"
    }

    @Test
    fun `echo -n suppresses trailing newline`() {
        run("echo", "-n", "hello") shouldBe "hello"
    }

    @Test
    fun `echo -n with no further args prints nothing`() {
        run("echo", "-n") shouldBe ""
    }

    @Test
    fun `echo single argument`() {
        run("echo", "test") shouldBe "test\n"
    }
}
