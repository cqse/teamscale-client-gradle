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
