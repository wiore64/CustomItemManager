# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-07-04

This is a minor update for the plugin, mostly updating the underlying structure.

### Changed

- Updated serialization/deserialization system to address future maintainability.
- Added a guide for item configuration, as well as to 'items.yml' itself.

## [1.0.1] - 2026-07-03

This is a patch for the plugin, addressing a few issues.

### Fixed

- The update checker used to log the current and latest versions, which was an unintended behavior. From now on, it
  does not do it anymore, only notifying you whether there is an update or not.
- When no items remained in the items.yml after executing /itemmanager remove, the YAML parser tended to write {} to
  the file, which was undesirable. Thus, that was fixed, and that does not happen anymore.

### Changed

- Instead of using an error-prone way of checking for the material specified in the items.yml config, now a safer and
  generally better approach is used, allowing for a more reliable checking of the specified item material.

## [1.0.0] - 2026-07-02

This is the first release version of the plugin.

### Added

- Command management system.
- Configuration management system.
- Custom item storage management system.
- Update checking utility.
- API for a future use by other plugins.
- PLACEHOLDERS.md to help with configuring the message config.