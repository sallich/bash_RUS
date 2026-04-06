package ru.bash

import kotlinx.coroutines.runBlocking
import ru.bash.commands.impl.ShellExitException
import ru.bash.executor.PipelineExecutor
import ru.bash.executor.PipelineResult
import ru.bash.semantic.RuntimeBuildVisitor
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.lexer.Lexer
import ru.bash.syntax.parser.Parser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class Shell(
    private val executor: PipelineExecutor,
    private val environment: Map<String, String> = System.getenv(),
    private val stdin: InputStream = System.`in`,
    private val stdout: OutputStream = System.out,
    private val stderr: OutputStream = System.err,
) {

    @Suppress("SwallowedException")
    fun run(): Unit = runBlocking {
        val reader = BufferedReader(InputStreamReader(stdin))
        printPrompt()
        var line = reader.readLine()
        while (line != null) {
            try {
                val result = executeLine(line)
                if (result.failed) {
                    stderr.write("exit status: ${result.lastExitCode}\n".toByteArray())
                    stderr.flush()
                }
            } catch (e: ShellExitException) {
                return@runBlocking
            } catch (e: ParseException) {
                stderr.write("bash: ${e.message}\n".toByteArray())
                stderr.flush()
            }
            printPrompt()
            line = reader.readLine()
        }
    }

    suspend fun executeLine(line: String): PipelineResult {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return PipelineResult(emptyList())
        val tokens = Lexer(trimmed).tokenize()
        val ast = Parser(tokens, trimmed).parse()
        val model = RuntimeBuildVisitor(environment).build(ast)
        return executor.execute(model, stdin, stdout)
    }

    private fun printPrompt() {
        stdout.write(PROMPT.toByteArray())
        stdout.flush()
    }

    private companion object {
        private const val PROMPT = "$ "
    }
}
