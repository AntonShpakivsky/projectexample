package service.locations

import service.RequestService

class ExampleRequestService : RequestService {
    override fun processed(message: String): String {
        return ""
    }
}