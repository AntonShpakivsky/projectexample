package processor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dto.request.Request
import dto.request.example.ExampleRequest
import dto.response.HeadResponse
import dto.response.Response
import dto.response.ResultResponse
import service.example.ExampleService
import utils.DEFAULT_ERROR_MESSAGE

class ExampleProcessor(private val exampleService: ExampleService) : Processor {
    private val objectMapper = jacksonObjectMapper()

    override fun process(request: String): String {
        return try {
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
            objectMapper.writeValueAsString(result)
        } catch (e: RuntimeException) {
            objectMapper.writeValueAsString(
                Response(
                    body = null,
                    head = HeadResponse(
                        result = ResultResponse.CRITICAL_ERROR,
                        userMessage = DEFAULT_ERROR_MESSAGE,
                        errorMessage = e.message,
                        operatorMessage = e.stackTraceToString(),
                    )
                )
            )
        }
    }
}