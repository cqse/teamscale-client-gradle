package eu.cqse.azure.api.model

import org.simpleframework.xml.Element

class Properties {
        @field:Element(name = "Content-Length", required = false)
        var contentLength: Int? = null
}