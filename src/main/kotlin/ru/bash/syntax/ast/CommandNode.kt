package ru.bash.syntax.ast

data class CommandNode(
    val name : String,
    val nodes : List<ArgumentNode>
) : AstNode
