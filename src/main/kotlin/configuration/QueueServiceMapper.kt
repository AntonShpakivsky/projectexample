package configuration

import service.locations.ExampleRequestService

object QueueServiceMapper {
    private val exampleRequestService = ExampleRequestService()

    private val queueMap =
        mapOf(
            "ExampleRequest" to exampleRequestService::processed
        )

    fun getQueueName(service: String) = queueMap[service] ?: throw IllegalArgumentException("Invalid service: $service")
}