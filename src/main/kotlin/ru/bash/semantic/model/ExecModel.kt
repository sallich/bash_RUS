package ru.bash.semantic.model

data class ExecModel(val commands: List<ExecCommand>)

data class ExecCommand(
    val name : String,
    val args : List<String>
)
