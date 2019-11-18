import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.Specification

class MultiProject extends Specification {
    def "Test multi project setup"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(new File("../"))
            .withArguments('-PprojectsDirName=test/data/multi-project', 'test')
			.forwardOutput()
            .build()

        then:
		result.tasks.collect{ it.path } == [':test', ':proj:test', ':sub:test', ':sub2:test', ':sub2:subsub:test']
		result.output.contains('Additional out from proj')
    }
}