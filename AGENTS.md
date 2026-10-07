# Repository Guidelines

## Project Structure

- `core/`: backend-neutral RHI API.
- `backend/mock/`, `backend/vulkan/`, `backend/webgpu/`: backend implementations.
- `samples/common/`: shared sample utilities.
- `samples/triangle/`: cross-platform triangle example application.
- `src/commonMain/kotlin`: shared code.
- `src/{apple,linux,mingw,web}Main`: platform-specific code.
- `samples/triangle/src/webMain/resources`: web assets.
- `gradle/libs.versions.toml`: dependency versions.

Keep backend-specific types out of `core`. Place target code in the narrowest applicable source set.

## Build, Test, and Development Commands

Run commands from the repository root. Use the Gradle wrapper; the daemon and CI use JDK 25.

- `make check`: run standard checks.
- `make test`: run multiplatform tests on available targets.
- `make format`: apply KtLint formatting and sort dependencies.
- `make docs`: generate Dokka API documentation in `build/dokka/html`.
- `make abi`: regenerate Kotlin ABI snapshots after intentional public API changes; review the diff.
- `make publish-local`: publish artifacts to Maven Local for consumer testing.

Pass additional options with `make check GRADLE_ARGS="--info"`.

- Run Make targets that invoke Gradle and direct `./gradlew` commands outside the filesystem sandbox so Gradle can access its cache.
- `./gradlew :core:check`: run checks for one module; replace `core` as needed.
- `./gradlew :samples:triangle:runDebugExecutable`: run the native triangle sample on a compatible host.

### Apple Vulkan Drivers

- Use KosmicKrisp by default for macOS development, sample execution, testing, and validation.
- `VulkanRenderer(enablePortability = true)` explicitly selects MoltenVK on macOS; iOS always selects MoltenVK.
- Driver selection filters physical devices before compatibility checks and never falls back to another driver.
- Bundle the runtime libraries and manifests for standalone execution. `VK_DRIVER_FILES` may explicitly override discovery. Driver selection still applies.

## Style & Naming

- Follow `.editorconfig` and IntelliJ ktlint style.
- Use UTF-8, LF endings, four-space indentation, and a 140-character line limit.
- Use two-space indentation for HTML and YAML.
- Allow trailing commas; avoid wildcard imports and trailing whitespace.
- Use `PascalCase` for types and `camelCase` for functions/properties.
- Root packages at `io.technoirlab.rhi`.
- Match filenames to primary types, e.g. `VulkanDevice.kt`.

## Testing

- Add shared tests to `<module>/src/commonTest/kotlin`.
- Use platform test source sets only for platform-specific behavior.
- Name test classes after subjects, e.g. `MockDeviceTest`.
- Test observable behavior; favor the mock backend where practical.
- Run `make check` before submitting changes.

## Commits and Pull Requests

- Use descriptive branch names without AI harness prefixes (such as `codex/`, `claude/`, `cursor/`, or `junie/`).
- Keep commits focused and use short, imperative commit subjects.
- Do not add a `Co-Authored-By` trailer.
- PR descriptions should explain the problem, the changes made, and the resulting behavior. Include compatibility impacts, remaining limitations, and links to related issues when relevant. Do not include checks performed, validation commands, or validation results.
- Separate refactors from behavior changes.
- Describe affected targets and backends.
- Include screenshots or recordings for rendering changes.
