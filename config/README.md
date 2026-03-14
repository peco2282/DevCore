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

### Loading and Saving Configurations

```kotlin
// Load (config.yml)
val config = Configs.load<MyConfig>(plugin)

// Load from a specific file
val otherConfig = Configs.load<OtherConfig>(File(plugin.dataFolder, "other.yml"))

// Save
Configs.save(plugin, config)
```

### Validation Annotations

- `@Comment(text)`: Specifies the comment to be output to the YAML.
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
- `@URL`: Validates that it is in a valid URL format.
- `@FileExists`: Validates that the file at the specified path exists.

