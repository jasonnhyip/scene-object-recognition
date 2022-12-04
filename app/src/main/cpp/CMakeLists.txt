cmake_minimum_required(VERSION 3.4.1)

include_directories(
        darknet/src/
        darknet/include/
)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wno-unknown-pragmas -Wfatal-errors -fPIC")
#neno opt
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}  -O3 -fstrict-aliasing")
#openmp opt
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}   -fexceptions -Dlinux -fsigned-char")

set(CMAKE_LD_FLAGS "${CMAKE_LD_FLAGS} -lm -pthread ")

file(GLOB darknet_files "darknet/src/*.c")

set(DARKNET_SRC_LISTS
    ${darknet_files}
        darknetlib.c
)

add_library(darknetlib SHARED
            darknetlib.c
            ${DARKNET_SRC_LISTS}
        )


find_library(   log-lib
                log )

# Include libraries needed for hello-jni lib
target_link_libraries(  darknetlib
                        jnigraphics
                        ${log-lib} )
