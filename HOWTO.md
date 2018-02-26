## Get FileVersion of an assembly

	tga.pdb.inbox.version = { dir ->
		new ByteArrayOutputStream().withStream { os ->
			exec {
				workingDir dir
				commandLine "Powershell", "(get-item some.dll).VersionInfo.FileVersion"
				standardOutput = os
			}
			os.toString().trim()
		}
	}

## Category traces based on subfolder structure, creates separate partition and disting message

	tga.trace.store.partition = { trace ->
		def app = trace.parentFile.parentFile.name
		// sanity check for traces that are in no subfolder
		if (app == "store") {
			app = trace.parentFile.name
		}
		def type = "Manual Test"
		if (trace.parentFile.name == "jenkins") {
			type = "Automated Jenkins Test"
		}

		return "$app $type"
	}

## Use an arbitrary version assembly matching a regular expression

	tga.trace.store.versionAssembly = ~/Foo\.Bar\..*/

## Process large files / increase Java VM more memory

For uploading large files it may be necessary to increase the JVM memory. Call the following before executing `gradlew`.

Windows:

	set JVM_OPTS=-Xmx6G

Linux:

	export JVM_OPTS=-Xmx6G
