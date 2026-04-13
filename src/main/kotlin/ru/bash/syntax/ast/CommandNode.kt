package ru.bash.syntax.ast

data class CommandNode(
    val name: ArgumentNode,
    val nodes: List<ArgumentNode>
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitCommand(this)
}
