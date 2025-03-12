package dto.response

data class Response<T>(
    val body: T,
    val head: HeadResponse
)

data class HeadResponse(
    val result: ResultResponse,
    val userMessage: String? = null,
    val errorMessage: String? = null,
    val operatorMessage: String? = null
)

enum class ResultResponse {
    OK,
    ERROR,
    CRITICAL_ERROR
}