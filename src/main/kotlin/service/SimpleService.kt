package service

import dto.response.Response
import dto.response.ResultResponse

interface SimpleService<T1, T2> {
    fun processRequest(request: T1): Response<T2>
    fun validateRequest(request: T1)
    fun handleSuccess(request: T1, response: Any? = null): Response<T2>
    fun handleError(
        error: Throwable,
        request: T1,
        resultResponse: ResultResponse,
        userMessage: String,
    ): Response<T2>
}