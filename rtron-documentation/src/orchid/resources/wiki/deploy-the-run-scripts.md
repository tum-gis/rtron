---
---

# Deploy the Run Scripts

In order to run the r:trån scripts in deployment environments, [kscript](https://github.com/holgerbrandl/kscript) needs to be installed.
kscript provides enhanced scripting support for Kotlin and is capable of executing the *.kts scripts contained in this [directory](rtron-main/src/main/resources/scripts).

To execute the scripts, you can either install kscript directly on the system or use a Docker container that comes with kscript preinstalled.

## Via a direct kscript installation

[sdkman](https://sdkman.io/install) is a tool for managing software development kits and conveniently installs [kotlin](https://kotlinlang.org/) and [kscript](https://github.com/holgerbrandl/kscript#installation):
```bash
curl -s "https://get.sdkman.io" | bash     # install sdkman
source "$HOME/.sdkman/bin/sdkman-init.sh"  # add sdkman to PATH

sdk install java # install java
sdk install kotlin # install Kotlin
sdk install kscript # install kscript
```
If you are on Windows, the deployment via docker is recommended.

Once the environment is ready, the r:trån scripts can be executed:
```bash
# download the script ...
curl https://raw.githubusercontent.com/tum-gis/rtron/main/rtron-main/src/main/resources/scripts/convert-opendrive-to-citygml2-simple.kts --output convert-opendrive-to-citygml2-simple.kts

# and simply execute it (dependencies are resolved automatically)
kscript ./convert-opendrive-to-citygml2-simple.kts
```

## Via Docker

With a [docker installation](https://docs.docker.com/get-docker/), the run scripts can be executed using the [r:trån container](https://hub.docker.com/r/rtron/rtron).
Adjust ``/adjust/path/...`` to your host system's paths and execute the following command:
```bash
docker run -v /adjust/path/to/input-datasets:/project/input \
           -v /adjust/path/to/output-datasets:/project/output \
           -i rtron/rtron - < /adjust/path/to/convert-opendrive-to-citygml2-simple.kts
```
Also note that the script must now reference paths in the container file system (``/project/input``, ``/project/output``).
