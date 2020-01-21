CHANGELOG
=========

Gives an overview of changes to the configuration format.
Especially note that *Renamed Properties* are incompatible changes that have to be propagated to existing configuration files on update.

## v20.01.0

### Support for multi-project builds

* Improved support for Gradle multi-project builds with config inheritance & injection
* Project configuration in separate directory that can be versioned (default `projects`) and makes updating the script pack easier

*Update advise for legacy projects*: Move `build.gradle` to `projects` folder or set `projectsDirName` to empty string in `gradle.properties`.

## v17.12.0

### Renamed Properties

* tga.pdb.uploadCollate -> tga.pdb.upload.collate
* tga.pdb.timestamp -> tga.pdb.upload.timestamp
* tga.pdb.timestampFile -> tga.pdb.upload.timestampFile
* tga.pdb.store.version -> tga.pdb.upload.version
* tga.trace.store.message -> tga.trace.upload.message
* tga.trace.store.timestamp -> tga.trace.upload.timestamp
* tga.trace.store.versionAssembly -> tga.trace.upload.versionAssembly
* tga.trace.store.versionAttribute -> tga.trace.upload.versionAttribute
* tga.trace.store.version -> tga.trace.upload.version
* tga.trace.store.partition -> tga.trace.upload.partition
* report.store.format -> report.upload.format
* report.store.timestamp -> report.upload.timestamp
* report.store.message -> report.upload.message
* report.store.partition -> report.upload.partition

### New Properties

* tga.pdb.upload.project
* tga.trace.upload.project
* tga.trace.upload.bucket
* tga.trace.upload.collate
