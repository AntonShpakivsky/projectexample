package service

import database.repository.ExampleRepository
import dto.request.example.ExampleRequest
import dto.response.ResultResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import service.example.ExampleService
import utils.DEFAULT_ERROR_MESSAGE
import utils.ValidateDataException
import java.util.UUID

class ExampleServiceTest : FreeSpec({
    val mockRepository = mockk<ExampleRepository>()
    val exampleService = ExampleService(mockRepository)

    "Service Example: success" {
        val idExample = UUID.fromString("84354cd4-c905-46fb-910f-345a2d07e1da")
        val request = ExampleRequest(idExample = idExample)

        val expectedName = "Example Name"
        every { mockRepository.findNameById(idExample) } returns expectedName

        val response = exampleService.processRequest(request)

        response.head.result shouldBe ResultResponse.OK
        response.body.idExample shouldBe idExample
        response.body.name shouldBe expectedName
    }

    "Service Example: validate error" {
        val request = ExampleRequest(idExample = null)

        val exception = shouldThrow<ValidateDataException> {
            exampleService.validateRequest(request)
        }

        exception.message shouldBe "Значение поля idExample пришло пустым"
    }

    "Service Example: database error" {
        val idExample = UUID.fromString("84354cd4-c905-46fb-910f-345a2d07e1da")
        val request = ExampleRequest(idExample = idExample)
        every { mockRepository.findNameById(idExample) } throws RuntimeException("Ошибка БД")

        val response = exampleService.processRequest(request)

        response.head.result shouldBe ResultResponse.CRITICAL_ERROR
        response.head.userMessage shouldBe DEFAULT_ERROR_MESSAGE
        response.head.errorMessage shouldBe "Ошибка БД"
    }
})
