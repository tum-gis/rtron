---
---

# Deploy the Run Scripts

In order to run the r:trån scripts in deployment environments, [kscript](https://github.com/holgerbrandl/kscript) needs to be installed.
kscript provides enhanced scripting support for Kotlin and is capable of executing the *.kts scripts contained in this [directory](rtron-main/src/main/resources/scripts).

[sdkman](https://sdkman.io/install) is a tool for managing software development kits and conveniently installs [kotlin](https://kotlinlang.org/) and [kscript](https://github.com/holgerbrandl/kscript#installation):
```bash
curl -s "https://get.sdkman.io" | bash     # install sdkman
source "$HOME/.sdkman/bin/sdkman-init.sh"  # add sdkman to PATH

sdk install java # install java
sdk install kotlin # install Kotlin
sdk install kscript # install kscript
```
If you are on Windows, kscript can be installed into a [Windows Subsystem for Linux (WSL)](https://docs.microsoft.com/en-us/windows/wsl/install-win10).
Otherwise, [docker containers](https://github.com/holgerbrandl/kscript#run-with-docker) for kscript are also available.

Once the environment is ready, the r:trån scripts can be executed:
```bash
# download the script ...
curl https://raw.githubusercontent.com/tum-gis/rtron/main/rtron-main/src/main/resources/scripts/convert-opendrive-to-citygml2-simple.kts --output convert-opendrive-to-citygml2-simple.kts

# and simply execute it (dependencies are resolved automatically)
kscript ./convert-opendrive-to-citygml2-simple.kts
```
