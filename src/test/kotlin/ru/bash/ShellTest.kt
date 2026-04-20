package ru.bash

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.bash.commands.impl.CatCommand
import ru.bash.commands.impl.CdCommand
import ru.bash.commands.impl.CommandRegistryImpl
import ru.bash.commands.impl.EchoCommand
import ru.bash.commands.impl.ExitCommand
import ru.bash.commands.impl.PwdCommand
import ru.bash.commands.impl.WcCommand
import ru.bash.commands.impl.LsCommand
import ru.bash.executor.PipelineExecutor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.writeText

class ShellTest {

    private val registry = CommandRegistryImpl(
        listOf(EchoCommand(), PwdCommand(), CatCommand(), ExitCommand(), WcCommand(), LsCommand())
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
<<<<<<< HEAD
<<<<<<< HEAD
    fun `run cd and pwd`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("cd | pwd")
        result.exitCodes shouldBe listOf(0, 0)
        out.toString() shouldBe System.getProperty("user.home") + "\n"
=======
=======
>>>>>>> 1d91743 (cd fix)
    fun `executeLine ls lists directory contents`(@TempDir dir: Path): Unit = runBlocking {
        dir.resolve("a.txt").writeText("")
        dir.resolve("b.txt").writeText("")

        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("ls $dir")
        result.failed shouldBe false
        out.toString() shouldBe "a.txt\nb.txt\n"
    }

    @Test
    fun `executeLine pipeline ls to wc -l`(@TempDir dir: Path): Unit = runBlocking {
        dir.resolve("a").writeText("")
        dir.resolve("b").writeText("")
        dir.resolve("c").writeText("")

        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("ls $dir | wc -l")
        result.exitCodes shouldBe listOf(0, 0)
        out.toString().trim() shouldBe "3"
<<<<<<< HEAD
>>>>>>> 44d502f (ls command)
=======
=======
    fun `run cd and pwd`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = shell(out).executeLine("cd | pwd")
        result.exitCodes shouldBe listOf(0, 0)
        out.toString() shouldBe System.getProperty("user.home") + "\n"
>>>>>>> a506fd7 (cd fix)
>>>>>>> 1d91743 (cd fix)
    }

}
