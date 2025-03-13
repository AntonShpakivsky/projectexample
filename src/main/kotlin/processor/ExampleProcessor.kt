package processor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dto.request.Request
import dto.request.example.ExampleRequest
import service.example.ExampleService

class ExampleProcessor(private val exampleService: ExampleService) : Processor {
    private val objectMapper = jacksonObjectMapper()

    override fun process(request: String): String {
        val parsedRequest: Request<*> = objectMapper.readValue(request)
        val result =
            when (parsedRequest.head.type) {
                "EXAMPLE" -> {
                    val body = objectMapper.convertValue(parsedRequest.body, ExampleRequest::class.java)
                    exampleService.processRequest(body)
                }

                else -> {
                    throw IllegalArgumentException("<65cf7211> Неизвестный тип запроса: ${parsedRequest.head.type}")
                }
            }
        return objectMapper.writeValueAsString(result)
    }
}