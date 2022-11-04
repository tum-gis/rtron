// user interface layer
include("rtron-cli")

// batch processing layer
include("rtron-main")

// single model processing layer
include("rtron-model", "rtron-readerwriter", "rtron-transformer")

// utility layer
include("rtron-std", "rtron-io", "rtron-math")
