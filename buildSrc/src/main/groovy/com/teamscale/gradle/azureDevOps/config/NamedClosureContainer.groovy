package com.teamscale.gradle.azureDevOps.config

/**
 * You can have a gradle DSL container were the contents of the form `<name> {}`
 * are used to created an Object, where the name is <name> and any properties can be set within the closure.
 *
 * Replicates roughly the behaviour of #NamedDomainObjectContainer, with the difference, that they can
 * be stacked inside of each other.
 */
abstract class NamedClosureContainer {

	String toString() {
		def props = new HashMap<>(this.properties)
		props.remove("class")
		return props
	}

	/**
	 * Return the instance which should be created using the name
	 * and filled with the closure
	 */
	protected abstract Object getNewInstance(String name)

	def methodMissing(String name, args) {
		if (args.size() > 0 && args[0] instanceof Closure) {
			args[0].delegate = getNewInstance(name)
			args[0].resolveStrategy = Closure.DELEGATE_ONLY
			args[0].run()
		} else {
			throw new MissingMethodException(name, this.class, args)
		}
	}
}
