package ru.bash

import ru.bash.commands.impl.*
import ru.bash.executor.PipelineExecutor

fun main() {
    val registry = CommandRegistryImpl(
        listOf(EchoCommand(), PwdCommand(), CatCommand(), ExitCommand(), GrepCommand())
    )
    val executor = PipelineExecutor(registry)
    Shell(executor).run()
}
