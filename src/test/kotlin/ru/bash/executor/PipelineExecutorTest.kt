package ru.bash.executor

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.bash.commands.impl.CatCommand
import ru.bash.commands.impl.CommandRegistryImpl
import ru.bash.commands.impl.EchoCommand
import ru.bash.commands.impl.ExitCommand
import ru.bash.commands.impl.ShellExitException
import ru.bash.semantic.model.ExecCommand
import ru.bash.semantic.model.ExecPipeline
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class PipelineExecutorTest {

    private val registry = CommandRegistryImpl(listOf(EchoCommand(), CatCommand(), ExitCommand()))
    private val err = ByteArrayOutputStream()
    private val executor = PipelineExecutor(registry, err)

    private fun pipeline(vararg commands: List<String>) =
        ExecPipeline(commands.map { ExecCommand(it) })

    @Test
    fun `empty pipeline returns empty exit codes`(): Unit = runBlocking {
        val result = executor.execute(ExecPipeline(emptyList()))
        result.exitCodes shouldBe emptyList()
        result.failed shouldBe false
        result.lastExitCode shouldBe 0
    }

    @Test
    fun `single echo returns exit code 0 and correct output`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = executor.execute(
            pipeline(listOf("echo", "hello")),
            InputStream.nullInputStream(),
            out,
        )
        result.exitCodes shouldBe listOf(0)
        result.failed shouldBe false
        out.toString() shouldBe "hello\n"
    }

    @Test
    fun `single failing command`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = executor.execute(
            pipeline(listOf("cat", "/no/such/file")),
            InputStream.nullInputStream(),
            out,
        )
        result.lastExitCode shouldNotBe 0
        result.failed shouldBe true
        result.exitCodes.size shouldBe 1
    }

    @Test
    fun `pipeline echo to cat passes output through`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = executor.execute(
            pipeline(listOf("echo", "world"), listOf("cat")),
            InputStream.nullInputStream(),
            out,
        )
        result.exitCodes shouldBe listOf(0, 0)
        result.failed shouldBe false
        out.toString() shouldBe "world\n"
    }

    @Test
    fun `pipeline exit codes tracked per stage`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = executor.execute(
            pipeline(listOf("echo", "test"), listOf("cat", "/no/such/file")),
            InputStream.nullInputStream(),
            out,
        )
        result.exitCodes.size shouldBe 2
        result.exitCodes[0] shouldBe 0
        result.exitCodes[1] shouldNotBe 0
        result.failed shouldBe true
        result.lastExitCode shouldNotBe 0
    }

    @Test
    fun `three-stage pipeline chains correctly`(): Unit = runBlocking {
        val out = ByteArrayOutputStream()
        val result = executor.execute(
            pipeline(listOf("echo", "chained"), listOf("cat"), listOf("cat")),
            InputStream.nullInputStream(),
            out,
        )
        result.exitCodes shouldBe listOf(0, 0, 0)
        out.toString() shouldBe "chained\n"
    }

    @Test
    fun `cat reads from stdin with no args`(): Unit = runBlocking {
        val input = ByteArrayInputStream("from stdin\n".toByteArray())
        val out = ByteArrayOutputStream()
        val result = executor.execute(
            pipeline(listOf("cat")),
            input,
            out,
        )
        result.exitCodes shouldBe listOf(0)
        out.toString() shouldBe "from stdin\n"
    }

    @Test
    fun `ShellExitException propagates out of execute`() {
        assertThrows<ShellExitException> {
            runBlocking {
                executor.execute(
                    pipeline(listOf("exit", "42")),
                    InputStream.nullInputStream(),
                    OutputStream.nullOutputStream(),
                )
            }
        }.code shouldBe 42
    }

    @Test
    fun `failed result has correct ExitCode`(): Unit = runBlocking {
        val result = PipelineResult(listOf(0, 3, 0))
        result.failed shouldBe true
        result.lastExitCode shouldBe 0
    }
}
