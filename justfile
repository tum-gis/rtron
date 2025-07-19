default: lint build test doc

# format and lint
lint:
    ./gradlew ktlintFormat

# generate required code
generate:
    ./gradlew xjcGeneration

# compile the current package
build mode="develop":
    if [ "{{mode}}" = "develop" ]; then \
        ./gradlew build; \
    elif [ "{{mode}}" = "release" ]; then \
        ./gradlew shadowJar; \
    else \
        echo "Unknown mode: {{mode}}"; \
        exit 1; \
    fi

# run unit tests and check linting
test: generate
    ./gradlew check

# remove generated artifacts
clean:
    ./gradlew clean

# create documentation
doc: generate
    ./gradlew dokkaHtmlMultiModule

# start local webserver with documentation
serve-doc: doc
    miniserve ./build/dokka/htmlMultiModule --index index.html

# check dependencies for updates or similiar
check-dependencies:
    ./gradlew dependencyUpdates

# build a container of the project
build-container:
    docker build -t rtron .
