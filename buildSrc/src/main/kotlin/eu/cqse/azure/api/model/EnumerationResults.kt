package eu.cqse.azure.api.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(strict = false)
data class EnumerationResults(
        @field:Attribute(name = "ServiceEndpoint")
        var serviceEndpoint: String = "",
        @field:Attribute(name = "ShareName")
        var shareName: String = "",
        @field:Attribute(name = "DirectoryPath")
        var directoryPath: String = "",
        @field:Element(name = "Entries")
        var entries: FileShareEntries = FileShareEntries()
)
