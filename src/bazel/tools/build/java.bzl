"""JUnit5"""

def junit5_tests(name, runtime_deps, select_package):
    """Runs JUnit5 tests using the ConsoleLauncher
    """
    runtime_deps += [
        "@maven//:org_junit_platform_junit_platform_console_standalone",
    ]
    args = [
        "--fail-if-no-tests",
    ]
    for pkg in select_package:
        args += [
            "--select-package",
            pkg,
        ]
    native.java_test(
        name = name,
        args = args,
        main_class = "org.junit.platform.console.ConsoleLauncher",
        use_testrunner = False,
        runtime_deps = runtime_deps,
    )
