package ru.bash.semantic.model

sealed class Redirect {
    data class Out(val path: String, val append: Boolean = false) : Redirect()
    data class In(val path: String) : Redirect()
}

data class ExecPipeline(val commands: List<ExecCommand>)

data class ExecCommand(
    val argv: List<String>,
    val redirects: List<Redirect> = emptyList(),
) {
    init {
        require(argv.isNotEmpty()) { "argv must not be empty" }
    }

    val name: String get() = argv.first()
}
