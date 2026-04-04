package ru.bash.syntax.ast

data class PipelineNode(val nodes: List<CommandNode>) : AstNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitPipeline(this)
}
