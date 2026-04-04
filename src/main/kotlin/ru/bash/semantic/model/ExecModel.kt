package ru.bash.semantic.model

data class ExecPipeline(val commands: List<ExecCommand>)

data class ExecCommand(
    val argv: List<String>
)
{
    val name: String get() = argv.firstOrNull().orEmpty()
}
