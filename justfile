default: lint build test doc

# format and lint the project
lint:
    ./gradlew ktlintFormat

# generate code
generate:
    ./gradlew xjcGeneration

# compile the project
build mode="develop":
    if [ "{{mode}}" = "develop" ]; then \
        ./gradlew build; \
    elif [ "{{mode}}" = "release" ]; then \
        ./gradlew shadowJar; \
    else \
        echo "Unknown mode: {{mode}}"; \
        exit 1; \
    fi

# execute unit and integration tests
test: generate
    ./gradlew check

# remove generated artifacts
clean:
    ./gradlew clean

# build the documentation
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
