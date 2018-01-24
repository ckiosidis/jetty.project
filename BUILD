
java_library(
    name = "jetty_server",
    srcs = glob(["jetty-server/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ['-XepDisableAllChecks'],
    deps = [
        ":jetty_http",
        ":jetty_io",
        ":jetty_util",
        ":jetty_jmx",
        "@javax_servlet_api//jar",

    ],
)

java_library(
    name = "jetty_http",
    srcs = glob(["jetty-http/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ['-XepDisableAllChecks'],
    deps = [
        ":jetty_util",
        ":jetty_io"
    ],
)

java_library(
    name = "jetty_util",
    srcs = glob(["jetty-util/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ['-XepDisableAllChecks'],
    deps = [
        "@slf4j_api//jar",
        "@javax_servlet_api//jar",

    ],
)

java_library(
    name = "jetty_io",
    srcs = glob(["jetty-io/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ['-XepDisableAllChecks'],
    deps = [
        ":jetty_util",

    ],
)

java_library(
    name = "jetty_jmx",
    srcs = glob(["jetty-jmx/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ['-XepDisableAllChecks'],
    deps = [
        ":jetty_util",
    ],
)


java_library(
    name = "jetty_alpn",
    srcs = glob(["jetty-alpn/jetty-alpn-server/src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ['-XepDisableAllChecks'],
    deps = [
        ":jetty_util",
        ":jetty_server",
        ":jetty_io",
        "@apln_api//jar",
    ],
)
