#!/bin/bash

mkdir -p build && cd build
cmake ..
make

./fresnel_integral_sampler
./spiral_sampler
