/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.tool.Maven;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Request;

/**
 * @since 1.4
 */
class MultiReleaseJarTests {

	private static final Maven mvn = Maven.install("3.6.0", Paths.get("build", "test-tools"));

	@Test
	void checkDefault() throws Exception {
		var variant = "default";
		var expectedLines = List.of(".", //
			"+-- JUnit Vintage [OK]", //
			"| '-- VintageIntegrationTest [OK]", //
			"|   '-- successfulTest [OK]", //
			"'-- JUnit Jupiter [OK]", //
			"  '-- JupiterIntegrationTests [OK]", //
			"    +-- javaPlatformModuleSystemIsAvailable() [OK]", //
			"    +-- javaScriptingModuleIsAvailable() [OK]", //
			"    +-- moduleIsNamed() [OK]", //
			"    +-- packageName() [OK]", //
			"    '-- resolve() [OK]", //
			"", //
			"Test run finished after \\d+ ms", //
			"[         4 containers found      ]", //
			"[         0 containers skipped    ]", //
			"[         4 containers started    ]", //
			"[         0 containers aborted    ]", //
			"[         4 containers successful ]", //
			"[         0 containers failed     ]", //
			"[         6 tests found           ]", //
			"[         0 tests skipped         ]", //
			"[         6 tests started         ]", //
			"[         0 tests aborted         ]", //
			"[         6 tests successful      ]", //
			"[         0 tests failed          ]", //
			"" //
		);

		var result = mvn(variant, expectedLines);

		assertEquals(0, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD SUCCESS"));
	}

	@Test
	void checkNoScripting() throws Exception {
		var variant = "no-scripting";
		var expectedLines = List.of(".", //
			"+-- JUnit Vintage [OK]", //
			"| '-- VintageIntegrationTest [OK]", //
			"|   '-- successfulTest [OK]", //
			"'-- JUnit Jupiter [OK]", //
			"  '-- JupiterIntegrationTests [OK]", //
			"    +-- javaPlatformModuleSystemIsAvailable() [OK]", //
			"    \\Q+-- javaScriptingModuleIsAvailable() [X] Failed to evaluate condition" //
					+ " [org.junit.jupiter.engine.extension.ScriptExecutionCondition]:" //
					+ " Class `javax.script.ScriptEngine` is not loadable," //
					+ " script-based test execution is disabled. If the originating" //
					+ " cause is a `NoClassDefFoundError: javax/script/...` and the" //
					+ " underlying runtime environment is executed with an activated" //
					+ " module system (aka Jigsaw or JPMS) you need to add the" //
					+ " `java.scripting` module to the root modules via" //
					+ " `--add-modules ...,java.scripting`\\E", //
			"    +-- moduleIsNamed() [OK]", //
			"    +-- packageName() [OK]", //
			"    '-- resolve() [OK]", //
			"", //
			">> STACKTRACE >>", //
			"", //
			"Test run finished after \\d+ ms", //
			"[         4 containers found      ]", //
			"[         0 containers skipped    ]", //
			"[         4 containers started    ]", //
			"[         0 containers aborted    ]", //
			"[         4 containers successful ]", //
			"[         0 containers failed     ]", //
			"[         6 tests found           ]", //
			"[         0 tests skipped         ]", //
			"[         6 tests started         ]", //
			"[         0 tests aborted         ]", //
			"[         5 tests successful      ]", //
			"[         1 tests failed          ]", //
			"" //
		);
		var result = mvn(variant, expectedLines);

		assertEquals(1, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD FAILURE"));
	}

	private Result mvn(String variant, List<String> expectedLines) throws Exception {
		var result = Request.builder() //
				.setTool(mvn) //
				.setProject("multi-release-jar") //
				.addArguments("--show-version", "--errors", "--file", variant, "test") //
				.setTimeout(Duration.ofMinutes(2)) //
				.build() //
				.run();

		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		var workspace = Path.of("build/test-workspace/multi-release-jar", variant);
		var actualLines = Files.readAllLines(workspace.resolve("target/junit-platform/console-launcher.out.log"));
		assertLinesMatch(expectedLines, actualLines);

		return result;
	}

}