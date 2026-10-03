cmake_minimum_required(VERSION 3.10.0)
project(LTWLegacy VERSION 1.0.0 LANGUAGES C)

# LTW Legacy — Mirai's from-source legacy OpenGL (1.x/2.1) translation layer for
# Minecraft 1.8 – 1.16.4. Built from the GL4ES snapshot vendored two directories up.
#
#   third_party/LTWVLegacy/ltwlegacy/src/main/cpp/CMakeLists.txt   <- this file
#   third_party/LTWVLegacy/include                                 <- Khronos + gl4es headers
#   third_party/LTWVLegacy/src                                     <- translation layer
#
# See ../../../UPSTREAM.md for provenance.

get_filename_component(LTWLEGACY_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)

set(LTWLEGACY_INCLUDE_DIR "${LTWLEGACY_ROOT}/include")
set(LTWLEGACY_SRC_DIR "${LTWLEGACY_ROOT}/src")

if(NOT EXISTS "${LTWLEGACY_SRC_DIR}/config.h")
    message(FATAL_ERROR
            "LTW Legacy: vendored sources are missing at ${LTWLEGACY_ROOT}. "
            "Expected ${LTWLEGACY_SRC_DIR}/config.h to exist.")
endif()

# src/gl holds the translator itself, including gl/wrap (entry point wrappers) and
# gl/math (expression evaluator). GLOB_RECURSE is safe here: the tree is a pinned,
# read-only vendored snapshot, so the file set never changes underneath a build.
file(GLOB_RECURSE LTWLEGACY_SOURCES "${LTWLEGACY_SRC_DIR}/gl/*.c")

# hardext.c performs hardware capability detection. Upstream compiles it on every
# platform; the X11/GBM sibling sources in src/glx are not used on Android.
list(APPEND LTWLEGACY_SOURCES "${LTWLEGACY_SRC_DIR}/glx/hardext.c")

list(LENGTH LTWLEGACY_SOURCES LTWLEGACY_SOURCE_COUNT)
if(LTWLEGACY_SOURCE_COUNT LESS 50)
    message(FATAL_ERROR
            "LTW Legacy: only ${LTWLEGACY_SOURCE_COUNT} translation-layer sources were found "
            "under ${LTWLEGACY_SRC_DIR}/gl. The vendored snapshot looks incomplete.")
endif()

add_library(ltwlegacy SHARED ${LTWLEGACY_SOURCES})

target_include_directories(ltwlegacy PRIVATE
        "${LTWLEGACY_INCLUDE_DIR}"
        "${LTWLEGACY_SRC_DIR}"
        "${LTWLEGACY_SRC_DIR}/gl"
        "${LTWLEGACY_SRC_DIR}/glx")

# Mirrors the defines upstream GL4ES applies for an Android target. NOX11/NO_GBM
# drop the X11 and DRM/GBM back ends; DEFAULT_ES picks OpenGL ES 2 as the floor,
# which LIBGL_ES can raise to 3 at runtime on capable devices.
target_compile_definitions(ltwlegacy PRIVATE
        ANDROID
        NOX11
        NO_GBM
        DEFAULT_ES=2)

# Logs go to stdout, matching the launcher's process log capture. Android's log
# library is still linked because the loader uses it for fatal diagnostics.
target_link_libraries(ltwlegacy dl m log)

# The wrapper is opened with dlopen() and its entry points resolved by dlsym(),
# so the whole translation surface has to stay visible in the dynamic symbol
# table. Stripping or localising it would make the library unloadable.
set_target_properties(ltwlegacy PROPERTIES
        OUTPUT_NAME "ltwlegacy"
        BUILD_WITH_INSTALL_RPATH FALSE)
