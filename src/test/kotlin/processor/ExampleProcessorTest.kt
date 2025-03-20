package processor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dto.request.HeadRequest
import dto.request.Request
import dto.request.example.ExampleRequest
import dto.response.HeadResponse
import dto.response.Response
import dto.response.ResultResponse
import dto.response.example.ExampleResponse
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import service.example.ExampleService
import utils.DEFAULT_ERROR_MESSAGE
import java.util.UUID

class ExampleProcessorTest : FreeSpec({
    val mockExampleService = mockk<ExampleService>()
    val objectMapper = jacksonObjectMapper()
    val exampleProcessor = ExampleProcessor(mockExampleService)

    "Processor Example: success" {
        val requestJson =
            objectMapper.writeValueAsString(
                Request(
                    head = HeadRequest("EXAMPLE"),
                    body = ExampleRequest(UUID.fromString("84354cd4-c905-46fb-910f-345a2d07e1da"))
                )
            )

        every { mockExampleService.processRequest(any()) } returns
            Response(
                body = ExampleResponse(UUID.randomUUID(), "test1"),
                head = HeadResponse(result = ResultResponse.OK)
            )

        val resultJson = exampleProcessor.process(requestJson)
        val result: Response<ExampleResponse> = objectMapper.readValue(resultJson)

        result.head.result shouldBe ResultResponse.OK
        result.body.name shouldBe "test1"
    }

    "Processor Example: unknown request type" {
        val requestJson =
            objectMapper.writeValueAsString(
                Request<Any>(head = HeadRequest("UNKNOWN"))
            )

        val resultJson = exampleProcessor.process(requestJson)
        val result: Response<ExampleResponse> = objectMapper.readValue(resultJson)

        result.head.result shouldBe ResultResponse.CRITICAL_ERROR
        result.head.userMessage shouldBe DEFAULT_ERROR_MESSAGE
        result.head.errorMessage shouldBe "<65cf7211> Неизвестный тип запроса: UNKNOWN"
    }

    "Processor Example: the service returned an error" {
        val requestJson =
            objectMapper.writeValueAsString(
                Request(
                    head = HeadRequest("EXAMPLE"),
                    body = ExampleRequest(UUID.fromString("84354cd4-c905-46fb-910f-345a2d07e1da"))
                )
            )

        every { mockExampleService.processRequest(any()) } throws RuntimeException("Internal Error")

        val resultJson = exampleProcessor.process(requestJson)
        val result: Response<ExampleResponse> = objectMapper.readValue(resultJson)

        result.head.result shouldBe ResultResponse.CRITICAL_ERROR
        result.head.userMessage shouldBe DEFAULT_ERROR_MESSAGE
        result.head.errorMessage shouldBe "Internal Error"
    }
})