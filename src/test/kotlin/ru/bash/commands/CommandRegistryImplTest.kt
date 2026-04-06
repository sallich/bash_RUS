package ru.bash.commands

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import ru.bash.commands.impl.CommandRegistryImpl
import ru.bash.commands.impl.EchoCommand
import ru.bash.commands.impl.ExternalCommand
import ru.bash.commands.impl.PwdCommand

class CommandRegistryImplTest {

    private val registry = CommandRegistryImpl(listOf(EchoCommand(), PwdCommand()))

    @Test
    fun `isBuiltin returns true for registered command`() {
        registry.isBuiltin("echo") shouldBe true
        registry.isBuiltin("pwd") shouldBe true
    }

    @Test
    fun `isBuiltin returns false for unknown command`() {
        registry.isBuiltin("ls") shouldBe false
        registry.isBuiltin("grep") shouldBe false
    }

    @Test
    fun `resolve returns builtin for registered command`() {
        registry.resolve("echo").shouldBeInstanceOf<EchoCommand>()
    }

    @Test
    fun `resolve returns ExternalCommand for unknown command`() {
        val cmd = registry.resolve("ls")
        cmd.shouldBeInstanceOf<ExternalCommand>()
        cmd.name shouldBe "ls"
    }
}
