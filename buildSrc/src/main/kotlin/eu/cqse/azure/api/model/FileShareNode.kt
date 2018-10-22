package eu.cqse.azure.api.model

import org.simpleframework.xml.Element

abstract class FileShareNode(
        @field:Element(name = "Name", required = true)
        var name: String = "",
        @field:Element(name = "Properties")
        var properties: Properties = Properties()
)
