# DevCore Config

[English] | [[日本語](README.ja.md)]

Module for mapping YAML (e.g., `config.yml`) to Kotlin data classes and performing validation.

## Features

- Type-safe configuration management using Kotlin data classes
- Automatic insertion of comments into YAML files
- Annotation-based validation
- Support for nested classes, lists, and maps
- Automatic loading, saving, and reloading

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:config:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:config")
}
```

## Usage

### Defining Configuration Classes

```kotlin
@Comment("Main plugin configuration")
data class MyConfig(
  @Comment("Player name")
  @NotBlank
  val name: String = "Steve",

  @Comment("Level (1-100)")
  @Range(min = 1, max = 100)
  val level: Int = 1,

  @Comment("Whether it is enabled")
  val enabled: Boolean = true
)
```

Use `@ConfigKey` when the canonical YAML key differs from the Kotlin property
name. The annotated key is preserved for both reading and writing:

```kotlin
data class ServiceConfig(
  @ConfigKey("request-timeout")
  val requestTimeout: Int = 30
)
```

### Loading and Saving Configurations

```kotlin
// Load (config.yml)
val config = Configs.load<MyConfig>(plugin)

// Load from a specific file
val otherConfig = Configs.load<OtherConfig>(File(plugin.dataFolder, "other.yml"))

// Save
Configs.save(plugin, config)
```

### Layered Configuration Sources

Use `Configs.from` to resolve values from multiple sections. Sections are
ordered from highest to lowest priority. Missing keys fall through to later
sections and then to constructor defaults. Explicit values such as `0` and
`false` are not treated as missing.

```kotlin
val environmentOverrides =
  plugin.config.getConfigurationSection("environments.$environment")
val sharedDefaults = plugin.config.getConfigurationSection("defaults")

val service = Configs.from(environmentOverrides, sharedDefaults)
  .convert<ServiceConfig>()
```

Nested sections are merged recursively, and null sections are ignored.

### Normalizing Numeric Values

Clamp annotations normalize numeric values while loading. The normalized value
is used to construct the configuration object and is written back to YAML.

```kotlin
data class LimitsConfig(
  @Clamp(min = 0.0, max = 1.0)
  val ratio: Double = 0.5,

  @ClampAtLeast(1.0)
  val workers: Int = 1,

  @ClampAtMost(60.0)
  val timeout: Long = 30
)
```

Clamp annotations differ from validation annotations: clamp annotations correct
out-of-range values, while annotations such as `@Range`, `@Min`, and `@Max`
reject them. Normalization runs before validation.

### Cross-Property Validation

Implement `ValidatableConfig` for invariants involving multiple properties.
Custom validation runs after property validation and is also applied recursively
to nested configuration objects.

```kotlin
data class IntervalConfig(
  val minimum: Double,
  val maximum: Double
) : ValidatableConfig {
  override fun validate() {
    require(minimum <= maximum) {
      "minimum must not exceed maximum"
    }
  }
}
```

### Built-in Bukkit Serializers

`ItemStack`, `Location`, and `Vector` are supported without manual serializer
registration. They can be used directly in configuration data classes,
including nested objects and lists.

```kotlin
data class ArenaConfig(
  val spawn: Location,
  val direction: Vector,
  val checkpoints: List<Vector>,
  val icon: ItemStack
)
```

Locations use `world`, `x`, `y`, `z`, and optional `yaw` and `pitch` keys.
Vectors use `x`, `y`, and `z`. Both Bukkit-deserialized objects and ordinary
YAML maps or sections are accepted. Unknown worlds and missing coordinates are
reported as configuration errors.

```yaml
spawn:
  world: world
  x: 10.5
  y: 64.0
  z: -20.5
  yaw: 90.0
  pitch: 0.0
direction:
  x: 1.0
  y: 0.0
  z: -1.0
```

### Validation Annotations

- `@Comment(text)`: Specifies the comment to be output to the YAML.
- `@ConfigKey(value)`: Specifies the canonical YAML key used for reading and writing.
- `@Clamp(min, max)`: Clamps a numeric value to an inclusive range.
- `@ClampAtLeast(value)`: Raises a numeric value to the specified minimum.
- `@ClampAtMost(value)`: Lowers a numeric value to the specified maximum.
- `@NotBlank`: Validates that a string is not empty or blank.
- `@NotEmpty`: Validates that a string, collection, or map is not empty.
- `@Range(min, max)`: Validates that a numeric value is within the specified range.
- `@Size(min, max)`: Validates that the number of elements in a collection is within the range.
- `@Regex(pattern)`: Validates that a string matches the regular expression.
- `@Email`: Validates that a string is in email address format.
- `@Min(value)`, `@Max(value)`: Specifies the minimum and maximum values for a numeric value.
- `@Positive`: Validates that a numeric value is positive (greater than 0).
- `@Negative`: Validates that a numeric value is negative (less than 0).
- `@NonNegative`: Validates that a numeric value is 0 or greater.
- `@Finite`: Validates that a floating-point value is neither NaN nor infinite.
- `@URL`: Validates that it is in a valid URL format.
- `@FileExists`: Validates that the file at the specified path exists.
