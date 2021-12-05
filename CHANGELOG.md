<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# linear-app-plugin Changelog

## [Unreleased]

### Added

- Issue search with query
- Support for all future IDE versions

### Changed

- Upgrade `platformVersion` to `2021.3`
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.5.32`
- Dependencies - upgrade `apollo graphql` to `2.5.11`
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.19.0`
- Dependencies - upgrade `detekt-formatting` to `1.19.0`
- Dependencies - upgrade `org.jetbrains.intellij` to `1.3.0`
- Dependencies (GitHub Actions) - upgrade `actions/cache` to `2.1.7`
- Dependencies - upgrade `org.jetbrains.changelog` to `1.3.1`
- Dependencies (GitHub Actions) - upgrade `actions/checkout` to `v2.4.0`
- Dependencies - upgrade `org.jlleitschuh.gradle.ktlint` to `10.2.0`

## [1.0.0]

### Added

- Use JVM compatibility version from `gradle.properties`
- Support for build 212.*

### Changed

- Upgrade Gradle Wrapper to `7.2`
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.18.0`
- Dependencies - upgrade `org.jetbrains.changelog` to `1.2.1`
- Dependencies - upgrade `org.jetbrains.intellij` to `1.1.4`
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.5.21`
- Use Java 11
- GitHub Actions: Use Java 11

### Fixed

- IDE showing error for old request when a new request arrives for getIssues

## [1.0.0-beta01]

### Changed

- Dependencies (GitHub Actions) - upgrade `gradle/wrapper-validation-action` to `1.0.4`
- Dependencies (GitHub Actions) - upgrade `actions/upload-artifact` to `2.2.4`
- Dependencies - upgrade `apollo graphql` to `2.5.9`
- Dependencies - upgrade `org.jetbrains.intellij` plugin to `1.0`

## [1.0.0-alpha03]

### Added

- Persist configuration across IDE restarts

### Changed

- Dependencies (GitHub Actions) - upgrade `org.jetbrains.kotlin.jvm` to `1.5.10`
- Dependencies (GitHub Actions) - upgrade `actions/cache` to `2.1.6`

### Fixed

- Adds missing id in `create-pull-request` Github Action

## [1.0.0-alpha02]

### Added

- Update Task Status

### Changed

- Upgrade Gradle Wrapper to `7.0.2`
- Remove reference to the `jcenter()` from Gradle configuration file
- Dependencies - upgrade `io.gitlab.arturbosch.detekt` to `1.17.1`
- Dependencies - upgrade `detekt-formatting` to `1.17.1`
- Dependencies - upgrade `apollo graphql` to `2.5.7`
- Dependencies (GitHub Actions) - upgrade `actions/create-release` to `v1.1.4`
- Dependencies (GitHub Actions) - upgrade `actions/checkout` to `v2.3.4`
- Dependencies (GitHub Actions) - upgrade `actions/upload-release-asset` to `v1.0.2`
- Dependencies - upgrade `org.jetbrains.kotlin.jvm` to `1.5.0`

## [1.0.0-alpha01]

### Added

- Initial implementation of Linear.app tasks server
