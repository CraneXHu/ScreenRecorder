# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library(GifEncorder SHARED src/main/jni/gifencorder.cpp)

find_library( # Defines the name of the path variable that stores the
              # location of the NDK library.
              jnigraphics-lib

              # Specifies the name of the NDK library that
              # CMake needs to locate.
              jnigraphics )

target_link_libraries( # Specifies the target library.
                       GifEncorder

                       # Links the log library to the target library.
                       ${jnigraphics-lib} )