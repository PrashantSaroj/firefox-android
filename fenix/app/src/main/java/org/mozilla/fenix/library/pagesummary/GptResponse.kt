package org.mozilla.fenix.library.pagesummary


data class GptResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val content: String
)
