package ru.bash

import kotlinx.coroutines.runBlocking
import ru.bash.commands.impl.ShellExitException
import ru.bash.executor.PipelineExecutor
import ru.bash.executor.PipelineResult
import ru.bash.semantic.CommandSubstitutionRunner
import ru.bash.semantic.ExpansionContext
import ru.bash.semantic.RuntimeBuildVisitor
import ru.bash.semantic.ShellWordExpander
import ru.bash.syntax.ast.AssignNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.lexer.Lexer
import ru.bash.syntax.parser.Parser
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class Shell(
    private val executor: PipelineExecutor,
    environment: Map<String, String> = System.getenv(),
    private val stdin: InputStream = System.`in`,
    private val stdout: OutputStream = System.out,
    private val stderr: OutputStream = System.err,
) {
    private val environment: MutableMap<String, String> = HashMap(environment)
    private var lastExitCode: Int = 0

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
            } catch (_: ShellExitException) {
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
        val substitution = CommandSubstitutionRunner { inner -> captureSubstitution(inner) }
        return when (ast) {
            is AssignNode -> {
                val expander = ShellWordExpander(
                    ExpansionContext(environment, lastExitCode),
                    substitution
                )
                val value = expander.expand(ast.value)
                environment[ast.name] = value
                val result = PipelineResult(listOf(0))
                lastExitCode = result.lastExitCode
                result
            }
            is PipelineNode -> {
                val expander = ShellWordExpander(
                    ExpansionContext(environment, lastExitCode),
                    substitution
                )
                val model = RuntimeBuildVisitor(expander).build(ast)
                val result = executor.execute(model, stdin, stdout)
                lastExitCode = result.lastExitCode
                result
            }
            else -> error("Unexpected AST node: ${ast::class.simpleName}")
        }
    }

    private suspend fun captureSubstitution(innerLine: String): String {
        val trimmed = innerLine.trim()
        if (trimmed.isEmpty()) return ""
        val tokens = Lexer(trimmed).tokenize()
        val ast = Parser(tokens, trimmed).parse()
        val substitution = CommandSubstitutionRunner { captureSubstitution(it) }
        val expander = ShellWordExpander(
            ExpansionContext(environment, lastExitCode),
            substitution
        )
        return when (ast) {
            is AssignNode -> throw ParseException("Unsupported assignment inside \$()", 0)
            is PipelineNode -> {
                val model = RuntimeBuildVisitor(expander).build(ast)
                val out = ByteArrayOutputStream()
                executor.execute(model, ByteArrayInputStream(ByteArray(0)), out)
                stripTrailingNewlines(String(out.toByteArray(), StandardCharsets.UTF_8))
            }
            else -> error("Unexpected AST in command substitution")
        }
    }

    private fun stripTrailingNewlines(s: String): String {
        var t = s
        while (t.endsWith('\n')) {
            t = t.dropLast(1)
        }
        return t
    }

    private fun printPrompt() {
        stdout.write(PROMPT.toByteArray())
        stdout.flush()
    }

    private companion object {
        private const val PROMPT = "$ "
    }
}
