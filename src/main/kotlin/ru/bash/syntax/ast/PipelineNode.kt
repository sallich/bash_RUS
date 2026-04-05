package ru.bash.syntax.ast

data class PipelineNode(val nodes: List<CommandNode>) : AstNode
