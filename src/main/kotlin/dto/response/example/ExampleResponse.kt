package dto.response.example

import java.util.UUID

data class ExampleResponse(
    val idExample: UUID,
    val name: String? = null,
)