package ru.bash.commands.impl

import ru.bash.commands.Command
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class GrepCommand : Command {
    override val name = "grep"


    override fun execute(argv: List<String>, stdin: InputStream, stdout: OutputStream): Int {
        var exitCode = 1
        val (options, pattern, files) = parseArguments(argv)
        if (pattern == null && files.isEmpty()) {
            stdout.write("grep: [OPTION]... PATTERNS [FILE]...\n".toByteArray())
            exitCode = 2
            return exitCode
        }
        val searchingStrategy = when {
            options.addContext > 0 -> WithContextSearchingStrategy(options.addContext)
            options.countLines && files.isEmpty() -> DefaultFromStdInSearchStrategy()
            files.isEmpty() -> InteractiveFromStdInSearchingStrategy(stdout)
            else -> DefaultSearchingStrategy()
        }
        val outputStrategy = when {
            options.countLines -> CountLinesOutputStrategy(files.size > 1)
            options.filesMatches -> FilesWithMatchesOutputStrategy()
            files.isEmpty() -> EmptyOutputStrategy()
            else -> DefaultOutputStrategy(files.size > 1)
        }

        val regex = createRegex(pattern ?: "", options)
        if (files.isEmpty()) {
            val matches = searchingStrategy.search(stdin, regex)
            exitCode = outputStrategy.write(matches, null, stdout)
        } else {
            for (path in files) {
                val file = File(path)
                if (!file.exists()) {
                    stdout.write("grep: $path: No such file or directory\n".toByteArray())
                    exitCode = 2
                    continue
                }
                val stream = file.inputStream()
                val matches = searchingStrategy.search(stream, regex)
                val code = outputStrategy.write(matches, path, stdout)
                if (code == 0) exitCode = 0
            }
        }
        return exitCode
    }

    private fun parseArguments(argv: List<String>): Triple<Options, String?, List<String>> {
        var ignoreCase = false
        var countLines = false
        var filesMatches = false
        var wholeWord = false
        var addContext = 0
        var i = 0
        val others = mutableListOf<String>()
        val args = argv.drop(1)
        while (i < args.size) {
            when {
                args[i] == "-i" -> {
                    ignoreCase = true
                }

                args[i] == "-c" -> {
                    countLines = true
                }

                args[i] == "-l" -> {
                    filesMatches = true
                }

                args[i] == "-w" -> {
                    wholeWord = true
                }

                args[i] == "-A" && i + 1 < args.size -> {
                    addContext = args[++i].toIntOrNull() ?: 0
                }

                else -> others.add(args[i])
            }
            i++
        }
        if (others.isEmpty()) {
            return Triple(Options(ignoreCase, countLines, filesMatches, wholeWord, addContext), null, emptyList())
        }

        val pattern = others[0]
        val files = if (others.size > 1) others.drop(1) else emptyList()
        return Triple(Options(ignoreCase, countLines, filesMatches, wholeWord, addContext), pattern, files)
    }

    private fun createRegex(pattern: String, options: Options): Regex {
        val finalPattern = if (options.wholeWord) "\\b$pattern\\b" else pattern
        val option = if (options.ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
        return Regex(finalPattern, option)
    }

    private data class Options(
        val ignoreCase: Boolean = false,
        val countLines: Boolean = false,
        val filesMatches: Boolean = false,
        val wholeWord: Boolean = false,
        val addContext: Int = 0
    )

    private interface SearchingStrategy {
        fun search(stream: InputStream, regex: Regex): List<String>
    }

    private class DefaultSearchingStrategy : SearchingStrategy {
        override fun search(stream: InputStream, regex: Regex): List<String> {
            val matchingLines = mutableListOf<String>()
            stream.bufferedReader().forEachLine { line ->
                val match = regex.containsMatchIn(line)
                if (match) {
                    matchingLines.add(line)
                }
            }
            return matchingLines
        }
    }

    private class DefaultFromStdInSearchStrategy : SearchingStrategy {
        override fun search(stream: InputStream, regex: Regex): List<String> {
            val matchingLines = mutableListOf<String>()
            val reader = stream.bufferedReader()
            var line: String?
            while (true) {
                line = reader.readLine() ?: break
                val match = regex.containsMatchIn(line)
                if (match) {
                    matchingLines.add(line)
                }
            }
            return matchingLines
        }
    }

    private class InteractiveFromStdInSearchingStrategy(private val output: OutputStream) : SearchingStrategy {
        override fun search(stream: InputStream, regex: Regex): List<String> {
            var flag = false
            val reader = stream.bufferedReader()
            var line: String?
            while (true) {
                line = reader.readLine() ?: break
                val match = regex.containsMatchIn(line)
                if (match) {
                    flag = true
                    output.write("$line\n".toByteArray())
                }
            }
            return if (flag) listOf("") else emptyList()
        }
    }

    private class WithContextSearchingStrategy(private val context: Int) : SearchingStrategy {
        override fun search(stream: InputStream, regex: Regex): List<String> {
            var currentContext = 0
            val matchingLines = mutableListOf<String>()
            stream.bufferedReader().forEachLine { line ->
                val match = regex.containsMatchIn(line)
                if (match) {
                    matchingLines.add(line)
                    currentContext = context
                } else if (currentContext > 0) {
                    matchingLines.add(line)
                    currentContext--
                }
            }
            return matchingLines
        }
    }


    private interface OutputStrategy {
        fun write(lines: List<String>, fileName: String?, output: OutputStream): Int
    }

    private class DefaultOutputStrategy(private val multipleFiles: Boolean) : OutputStrategy {
        override fun write(lines: List<String>, fileName: String?, output: OutputStream): Int {
            if (lines.isEmpty()) {
                return 1
            }
            val prefix = when {
                fileName.isNullOrBlank() || !multipleFiles -> ""
                else -> "$fileName:"
            }
            for (line in lines) {
                output.write("$prefix$line\n".toByteArray())
            }
            return 0
        }
    }

    private class CountLinesOutputStrategy(private val multipleFiles: Boolean) : OutputStrategy {
        override fun write(lines: List<String>, fileName: String?, output: OutputStream): Int {
            if (lines.isEmpty()) {
                return 1
            }
            val prefix = when {
                fileName.isNullOrBlank() || !multipleFiles -> ""
                else -> "$fileName:"
            }
            output.write("$prefix${lines.size}\n".toByteArray())
            return 0
        }
    }

    private class FilesWithMatchesOutputStrategy : OutputStrategy {
        override fun write(lines: List<String>, fileName: String?, output: OutputStream): Int {
            if (lines.isNotEmpty()) {
                output.write("$fileName\n".toByteArray())
                return 0
            }
            return 1
        }
    }

    private class EmptyOutputStrategy : OutputStrategy {
        override fun write(lines: List<String>, fileName: String?, output: OutputStream): Int {
            if (lines.isEmpty()) {
                return 1
            }
            return 0
        }
    }
}