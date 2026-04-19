package ru.bash

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.bash.commands.impl.CatCommand
import ru.bash.commands.impl.CommandRegistryImpl
import ru.bash.commands.impl.EchoCommand
import ru.bash.commands.impl.ExitCommand
import ru.bash.commands.impl.PwdCommand
import ru.bash.commands.impl.WcCommand
import ru.bash.executor.PipelineExecutor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class ShellTest {

    private val registry = CommandRegistryImpl(
        listOf(EchoCommand(), PwdCommand(), CatCommand(), ExitCommand(), WcCommand())
    )
    private val err = ByteArrayOutputStream()
    private val executor = PipelineExecutor(registry, err)

    private fun shell(
        stdout: OutputStream = OutputStream.nullOutputStream(),
        env: Map<String, String> = emptyMap(),
    ) = Shell(executor, env, InputStream.nullInputStream(), stdout, err)

    @Test
    fun `executeLine echo returns success`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("echo hello")
        result.failed shouldBe false
        out.toString() shouldBe "hello\n"
    }

    @Test
    fun `executeLine empty line returns empty result`(): Unit = runBlocking {
        val result = shell().executeLine("   ")
        result.exitCodes shouldBe emptyList()
    }

    @Test
    fun `executeLine pipeline echo to cat`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("echo piped | cat")
        result.exitCodes shouldBe listOf(0, 0)
        out.toString() shouldBe "piped\n"
    }

    @Test
    fun `executeLine variable expansion`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val env = mapOf("GREETING" to "hi")
        val result = shell(out, env).executeLine("echo \$GREETING")
        result.failed shouldBe false
        out.toString() shouldBe "hi\n"
    }

    @Test
    fun `executeLine failed command tracked`(): Unit = runBlocking {
        val result = shell().executeLine("cat /no/such/file")
        result.failed shouldBe true
        result.lastExitCode shouldNotBe 0
    }

    @Test
    fun `run loop executes multiple commands`() {
        val input = ByteArrayInputStream("echo a\necho b\n".toByteArray())
        val out = ByteArrayOutputStream()
        Shell(executor, emptyMap(), input, out, err).run()
        out.toString() shouldContain "a\n"
        out.toString() shouldContain "b\n"
    }

    @Test
    fun `run stops on exit command`() {
        val input = ByteArrayInputStream("exit 0\necho never\n".toByteArray())
        val out = ByteArrayOutputStream()
        Shell(executor, emptyMap(), input, out, err).run()
        out.toString() shouldBe "$ "
    }

    @Test
    fun `run prints error on parse failure`() {
        val input = ByteArrayInputStream("echo test |\n".toByteArray())
        val errOut = ByteArrayOutputStream()
        Shell(executor, emptyMap(), input, OutputStream.nullOutputStream(), errOut).run()
        errOut.toString() shouldContain "bash:"
    }

    @Test
    fun `ShellExitException carries exit code`() {
        val input = ByteArrayInputStream("exit 5\n".toByteArray())
        val out = ByteArrayOutputStream()
        Shell(executor, emptyMap(), input, out, err).run()
        out.toString() shouldBe "$ "
    }

    @Test
    fun `run pipeline with echo and grep`() {
        val input = ByteArrayInputStream("echo aaa | grep a".toByteArray())
        val out = ByteArrayOutputStream()
        Shell(executor, emptyMap(), input, out, err).run()
        out.toString() shouldContain "aaa\n"
    }
    
    @Test
    fun `assignment returns exit code 0`(): Unit = runBlocking {
        val result = shell().executeLine("FOO=bar")
        result.failed shouldBe false
        result.exitCodes shouldBe listOf(0)
    }

    @Test
    fun `executeLine pipeline echo to wc -w`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("echo one two three | wc -w")
        result.exitCodes shouldBe listOf(0, 0)
        out.toString().trim() shouldBe "3"
    }

    @Test
    fun `executeLine dollar question expands last exit code`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val s = shell(out)
        s.executeLine("cat /no/such/file")
        val result = s.executeLine("echo " + "$" + "?")
        result.failed shouldBe false
        out.toString().trim() shouldBe "1"
    }

    @Test
    fun `executeLine arithmetic expansion`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("echo " + "$" + "((1+2*3))")
        result.failed shouldBe false
        out.toString() shouldBe "7\n"
    }

    @Test
    fun `executeLine command substitution`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("echo " + "$" + "(echo sub)")
        result.failed shouldBe false
        out.toString() shouldBe "sub\n"
    }

    @Test
    fun `executeLine nested command substitution`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("echo " + "$" + "(echo " + "$" + "(echo inner))")
        result.failed shouldBe false
        out.toString() shouldBe "inner\n"
    }

}
