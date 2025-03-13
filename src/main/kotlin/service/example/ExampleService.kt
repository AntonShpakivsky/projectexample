package service.example

import database.repository.ExampleRepository
import dto.request.example.ExampleRequest
import dto.response.HeadResponse
import dto.response.Response
import dto.response.ResultResponse
import dto.response.example.ExampleResponse
import service.SimpleService
import utils.DEFAULT_ERROR_MESSAGE
import utils.ValidateDataException

class ExampleService(private val repository: ExampleRepository) : SimpleService<ExampleRequest, ExampleResponse> {
    override fun processRequest(request: ExampleRequest): Response<ExampleResponse> {
        val result =
            try {
                validateRequest(request)
                val responseDB = repository.findNameById(request.idExample!!)
                handleSuccess(request, responseDB)
            } catch (e: ValidateDataException) {
                handleError(
                    e,
                    request,
                    ResultResponse.ERROR,
                    e.message ?: DEFAULT_ERROR_MESSAGE
                )
            } catch (e: RuntimeException) {
                handleError(e, request, ResultResponse.CRITICAL_ERROR, DEFAULT_ERROR_MESSAGE)
            }
        return result
    }

    override fun validateRequest(request: ExampleRequest) {
        if (request.idExample == null) {
            throw ValidateDataException("Значение поля idExample пришло пустым")
        }
    }

    override fun handleSuccess(
        request: ExampleRequest,
        response: Any?
    ): Response<ExampleResponse> {
        val nameExample = response as String?
        return Response(
            body =
                ExampleResponse(
                    idExample = request.idExample!!,
                    name = nameExample
                ),
            head = HeadResponse(result = ResultResponse.OK)
        )
    }

    override fun handleError(
        error: Throwable,
        request: ExampleRequest,
        resultResponse: ResultResponse,
        userMessage: String
    ): Response<ExampleResponse> {
        return Response(
            body = ExampleResponse(idExample = request.idExample!!),
            head =
                HeadResponse(
                    result = resultResponse,
                    userMessage = userMessage,
                    errorMessage = error.message,
                    operatorMessage = error.stackTraceToString()
                )
        )
    }
}