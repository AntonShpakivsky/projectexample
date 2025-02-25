package service

interface RequestService {
    fun processed(message: String): String
}