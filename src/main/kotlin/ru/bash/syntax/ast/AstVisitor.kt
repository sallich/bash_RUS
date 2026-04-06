package ru.bash.syntax.ast

interface AstVisitor<out R> {
    fun visitPipeline(node: PipelineNode): R
    fun visitCommand(node: CommandNode): R
    fun visitShellWord(node: ShellWordNode): R
    fun visitWord(node: WordNode): R
    fun visitString(node: StringNode): R
    fun visitSingleQuoted(node: SingleQuotedNode): R
    fun visitVariable(node: VariableNode): R
}
