<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# linear-app-plugin Changelog

## [Unreleased]
### Added
- Use JVM compatibility version from `gradle.properties`

### Changed
- Use Java 11
- GitHub Actions: Use Java 11

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
