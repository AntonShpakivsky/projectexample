package processor

interface Processor {
    fun process(request: String): String
}