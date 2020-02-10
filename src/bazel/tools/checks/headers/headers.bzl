"""License header check"""

def headers_check(header_type):
    prefix = native.package_name()
    paths = native.glob(["**/*.java"])
    full_paths = [prefix + "/" + f for f in paths]

    native.java_test(
        name = "headers_check",
        tags = ["check", "headers-check"],
        runtime_deps = ["//tools/checks/headers"],
        use_testrunner = False,
        main_class = "tools.checks.headers.HeadersChecker",
        data = paths,
        args = [header_type] + full_paths,
    )
