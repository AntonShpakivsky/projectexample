package dto.request

import java.util.UUID

data class Request<T>(
    val body: T? = null,
    val head: HeadRequest
)

data class HeadRequest(
    val type: String,
    val idClient: UUID? = null
)