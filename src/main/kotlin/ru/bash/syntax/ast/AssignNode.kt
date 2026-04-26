package ru.bash.syntax.ast

data class AssignNode(
    val name: String,
    val value: ArgumentNode,
) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitAssign(this)
}
