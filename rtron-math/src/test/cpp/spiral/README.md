# Spiral Test

Small program for sampling the Fresnel integral and the spiral implementation
used in [ASAM OpenDRIVE](https://www.asam.net/standards/detail/opendrive/).

## Prerequisites

Install the build tools:

```bash
sudo apt-get install cmake build-essential
```

## Building

Build the programs using CMake:

```bash
mkdir -p build && cd build
cmake ..
make
```

## Running

Run both executables:

```bash
./fresnel_integral_sampler
./spiral_sampler
```

Or, compile and run it via the script:

```bash
./compile_run.sh
```

## Acknowledgements

Thanks to:
- Stephen L. Moshier for the Fresnel integral implementation in the [CEPHES](http://www.netlib.org/cephes/) library
- [ASAM](https://www.asam.net) for providing the odrSpiral implementation
