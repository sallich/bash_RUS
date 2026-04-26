package ru.bash.commands

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.bash.commands.impl.ExitCommand
import ru.bash.commands.impl.ShellExitException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExitCommandTest {

    private val exit = ExitCommand()
    private val emptyStdin = ByteArrayInputStream(ByteArray(0))
    private val out = ByteArrayOutputStream()

    @Test
    fun `exit with no args throws ShellExitException with code 0`() {
        val ex = assertThrows<ShellExitException> {
            exit.execute(listOf("exit"), emptyStdin, out, ByteArrayOutputStream())
        }
        ex.code shouldBe 0
    }

    @Test
    fun `exit with code throws ShellExitException with that code`() {
        val ex = assertThrows<ShellExitException> {
            exit.execute(listOf("exit", "42"), emptyStdin, out, ByteArrayOutputStream())
        }
        ex.code shouldBe 42
    }

    @Test
    fun `exit with non-numeric arg throws IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            exit.execute(listOf("exit", "abc"), emptyStdin, out, ByteArrayOutputStream())
        }
    }

    @Test
    fun `exit rejects more than one argument`() {
        assertThrows<IllegalArgumentException> {
            exit.execute(listOf("exit", "1", "2"), emptyStdin, out, ByteArrayOutputStream())
        }
    }
}
