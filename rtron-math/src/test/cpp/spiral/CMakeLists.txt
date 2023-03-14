cmake_minimum_required(VERSION 3.1)

project(OpendriveSpiralTest C)

add_executable(fresnel_integral_sampler src/odrSpiral/odrSpiral.h src/odrSpiral/odrSpiral.c src/fresnel_integral_sampler_main.c)
target_link_libraries(fresnel_integral_sampler m)

add_executable(spiral_sampler src/odrSpiral/odrSpiral.h src/odrSpiral/odrSpiral.c src/spiral_sampler_main.c)
target_link_libraries(spiral_sampler m)

