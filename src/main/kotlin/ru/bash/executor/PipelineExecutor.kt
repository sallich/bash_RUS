package ru.bash.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.bash.commands.CommandRegistry
import ru.bash.semantic.model.ExecPipeline
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class PipelineExecutor(
    private val registry: CommandRegistry,
    private val stderr: OutputStream = System.err,
) {

    suspend fun execute(
        pipeline: ExecPipeline,
        stdin: InputStream = System.`in`,
        stdout: OutputStream = System.out,
    ): PipelineResult = coroutineScope {
        val commands = pipeline.commands
        if (commands.isEmpty()) return@coroutineScope PipelineResult(emptyList())

        val n = commands.size
        val pipeOuts = Array(n - 1) { PipedOutputStream() }
        val pipeIns = Array(n - 1) { i -> PipedInputStream(pipeOuts[i]) }

        val deferred = commands.mapIndexed { i, cmd ->
            val cmdIn: InputStream = if (i == 0) stdin else pipeIns[i - 1]
            val cmdOut: OutputStream = if (i < n - 1) pipeOuts[i] else stdout

            async(Dispatchers.IO) {
                try {
                    registry.resolve(cmd.name).execute(cmd.argv, cmdIn, cmdOut)
                } catch (e: IOException) {
                    stderr.write("${cmd.name}: ${e.message}\n".toByteArray())
                    FAILURE_EXIT_CODE
                } catch (e: IllegalArgumentException) {
                    stderr.write("${cmd.name}: ${e.message}\n".toByteArray())
                    FAILURE_EXIT_CODE
                } finally {
                    if (cmdOut !== stdout) cmdOut.close()
                }
            }
        }

        PipelineResult(deferred.awaitAll())
    }

    private companion object {
        private const val FAILURE_EXIT_CODE = 1
    }
}
