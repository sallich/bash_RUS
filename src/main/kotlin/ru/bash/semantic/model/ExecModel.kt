package ru.bash.semantic.model

data class ExecPipeline(val commands: List<ExecCommand>)

data class ExecCommand(
    val argv: List<String>
) {
    init {
        require(argv.isNotEmpty()) { "argv must not be empty" }
    }

    val name: String get() = argv.first()
}
