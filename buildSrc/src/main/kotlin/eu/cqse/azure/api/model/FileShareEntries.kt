package eu.cqse.azure.api.model

import org.simpleframework.xml.ElementList

internal class FileShareEntries (
//    @ElementListUnion(
//        ElementList(entry = "File", inline = true, type = File::class),
//        ElementList(entry = "Directory", inline = true, type = Directory::class)
//    )
        @field:ElementList(entry = "File", inline = true, type = File::class, required = false)
    var files: ArrayList<File> = ArrayList(),
        @field:ElementList(entry = "Directory", inline = true, type = Directory::class, required = false)
    var directories: ArrayList<Directory> = ArrayList()
)
