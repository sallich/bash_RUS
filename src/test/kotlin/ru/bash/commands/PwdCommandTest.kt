package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.bash.commands.impl.PwdCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PwdCommandTest {

    private val pwd = PwdCommand()
    private val emptyStdin = ByteArrayInputStream(ByteArray(0))

    @Test
    fun `pwd prints current directory`() {
        val out = ByteArrayOutputStream()
        val code = pwd.execute(listOf("pwd"), emptyStdin, out)
        code shouldBe 0
        out.toString().trim() shouldBe System.getProperty("user.dir")
    }

    @Test
    fun `pwd output ends with newline`() {
        val out = ByteArrayOutputStream()
        pwd.execute(listOf("pwd"), emptyStdin, out)
        out.toString() shouldEndWith "\n"
    }

    @Test
    fun `pwd rejects arguments`() {
        assertThrows<IllegalArgumentException> {
            pwd.execute(listOf("pwd", "extra"), emptyStdin, ByteArrayOutputStream())
        }
    }
}
