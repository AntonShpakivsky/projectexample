package configuration

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import processor.ExampleProcessor

class ProcessorRegistry : KoinComponent {
    private val exampleProcessor: ExampleProcessor by inject()

    val processors =
        mapOf<String, (message: String) -> String>(
            "ExampleRequest" to { message -> exampleProcessor.process(message) }
        )
}