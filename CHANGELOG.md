# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project DOES NOT adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html),
but instead follows [Break Versioning](https://www.taoensso.com/break-versioning).

## Unreleased

### Added

* Automatic deps.edn dependency updates (via [lambdaisland/classpath](https://github.com/lambdaisland/classpath)).
* [Portal](https://github.com/djblue/portal) wrapper and automatic opening.

## 2611bdf - 2025-11-06 (and before)

### Added

* Start nREPL and socket repl.
* Load dev namespace (or `cljdev.dev` if not found).
* Optionally refresh using `clojure.tools.namespace.repl`.
