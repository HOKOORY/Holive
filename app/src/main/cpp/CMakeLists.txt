cmake_minimum_required(VERSION 3.22.1)
project("holive_native")

add_library(
    holive_native
    SHARED
    native_endpoints.cpp
)
