# Upload of ADOS Builds to Teamscale
Instruction for the configuration of the upload of ADOS builds. This includes the current build pipelines as well as 
the old `XAML` builds.
## Basic configuration
The following is a base configuration for all projects. The `subprojects {}` extension makes sure that every *gradle* 
file on the same level or below has the same configuration.  
If need be, the configuration can be changed or extended.
```
apply plugin: 'com.teamscale.gradle'
subprojects {
    teamscale {
        // Base config for the teamscale server
        server {
            url = <URL>
            username = <USER>
            accesskey = <KEY>
        }
    
        azureDevOps {
            // REQUIRED: Contains the json files which save which builds have been uploaded
            cacheDir "<CACHE-DIR-PATH>" 
            codeCoverageExe "<PATH-TO-EXE>" 
            coverageMergerExe "<PATH-TO-EXE>"

            options {
                // OPTIONAL: Build findings have an ID of a 2-3 characters followed by 4-5 numbers.
                // Here you can define which findings, based on the prefixes should be uploaded to teamscale 
                // For example, here, MSB0004 will not be uploaded, but SCS0004 will be
                csharpFindings = ["CS", "SCS"]
            }
        
            // Credentials for online Azure Dev Ops systems hosted on visualstudio.com
            credentials {
                // Here you can add credentials for different ADOS on visualstudio.com
                "apps-munichre" { # Credentials for https://apps-munichre.visualstudio.com/
                    username = "token"
                    password = <ACCESS-TOKEN>
                }
            }
        }
    }
}
```

### azureDevOps config
`codeCoverageExe "PATH"`: Required if uploading VS_COVERAGE (converts .coverage to .xml)  
Preferred as it only needs the .coverage. If this doesn't produce an output use MS_COVERAGE

`coverageMergerExe "<PATH-TO-EXE>"`: Required for uploading MS_COVERAGE (converts .coverage to .xml)  
Some old coverage files can only be converted with this exe. PDBs and DLL must be included in the conversion process

`options { csharpFindings = ["CS", "SCS"] }`:  
Build findings have an ID of a 2-3 characters followed by 4-5 numbers.  
Here you can define which findings, based on the prefixes should be uploaded to teamscale 
For example, here, MSB0004 will not be uploaded, but SCS0004 will be.

#### XAML options
Everything inside `xaml {}`  
`inbox "PATH"`: Path to the inbox containing build archives  
`zipstore "PATH"`: Path to a folder which can be used to backup the already processed build files  
`maxStoredZips = 5`: Nmuber of builds which will be stored per build definition. Old ones will be deleted first.

## Project configuration

### Example
```
teamscale {
    server {
        // id/alias of the teamcsale project. `url`, `username` and `accesskey` are taken from the basic config 
        project = "cqse"
    }

    azureDevOps {
        // link to the build definition, for example: https://apps-munichre.visualstudio.com/EXAMPLE/_build?definitionId=136
        builds { 
            "apps-munichre" { // There must be a credentials entry with the same name
                "EXAMPLE" {
                    "136" { // id of the build definitions. You can use the name, but the id is safer in the case the definition is renamed
                        // Here you can add any ADOS build options
                        branchMapping = { source ->
                            if(source ==~ /master/) {
                                return ""
                            }
                            return null
                        }
    
                        tests {
                            result "MS_TEST", /.*\.trx/
                            result "JUNIT", /.*\.xml/, null, "Junit Tests"
                            coverage "VS_COVERAGE", /.*\.xml/, null, "TypeScript Tests"
                        }
                    }
                }
            }
        }

        xaml {
            "apps-munichre" {
                "EXAMPLE" {
                    "Nightly_Xaml_build" {
                        errors "BuildType/Errors.txt"
                        warnings "CSHARP", "BuildType/Warnings.txt"
                        result "MS_TEST", /TestResults\/.*\.trx/, "Test"
                        coverage "MS_COVERAGE", /TestResults\/.*\.(coverage|pdb|dll|orig)/, "Metrics" 
                    }
                }
            }
        }
    }
}
```

### ADOS Build Options
Options for builds. Optional parameter are marked with a ?

* `branchmapping = { source -> destination }`  
branchMapping can be used to filter which builds from which branches you want to use or where to upload the builds to. 
`source` is the name of the branch of the ADOS build. Returning `null` ignores the build, returning an `""` uploads the
build to the default branch.  
Returning any other string uploads the build to the branch with the name defined in the string.

* `parseLogs TYPE, /PATTERN/, PARTITION?`: Activates the parsing of the logs for any build findings.  
`TYPE` defines the log parser which must be included in the source code (ATM there is only `CSHARP`).  
`/PATTERN/` is a regex filtering which defines which log of which task step of a build should be parsed. The different
steps can be seen in a build under `Logs`
`PARTITION`: optional parameter which defines to which partition the build findings will be uploaded to

* `report TYPE, PATH, ARTIFACT, PARTITION?`: Upload any kind of report to teamscale
`TYPE`: Any `EReportFormat` in Teamscale
`PATH`: Regex of a path inside of an artifact
`ARTIFACT`: Regex for matching the name of an artifact. If `null` it will be ignored
`PARTITION`: optional parameter which defines to which partition the build findings will be uploaded to

#### Test Options
Any options inside of `tests {}` inside of the build options

* `result TYPE, PATH, ARTIFACT?, PARTITION?`: Options for uploading test results  
`TYPE`: Any `EReportFormat` in Teamscale  
`PATH`: Regex for matching the name of a test result (Will match a path in combination with artifact, otherwise only a name)  
`ARTIFACT`: Regex for matching the name of an artifact  
`PARTITION`: optional parameter which defines to which partition the build findings will be uploaded to

* `coverage TYPE, PATH, ARTIFACT?, PARTITION?`: Options for upload code coverage  
Same as `result`

* `release TYPE, PATH, ARTIFACT?, PARTITION?`: Options for uploading test results for releases of builds  
Same as `result`

### Xaml Options
Options for the old xaml build. Prerequisite is that we are getting the build archives in our inbox  
* `errors PATH`: Path to the file containing the build errors (can be a regex as well)  
* `warnings PATH`: Path to the file containing the build warnings (can be a regex as well)  
* `result TYPE, PATH, PARTITION?`: Options for uploading test results  
Same as above
* `coverage TYPE, PATH, PARTITION?`: Options for uploading code coverage 
* `timestamp PATH`: Path to the build log where the timestamp can be parsed out of.  
This should be done if the timestamp of the build cannot be parsed out of the name of the build archive.
* `uploadToBranch NAME`: Name of the branch where the results should be uploaded to 
