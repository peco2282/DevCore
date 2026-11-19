# Config migrations

Mark the integer schema version and declare every adjacent upgrade. The default value on the annotated property is the target version.

```kotlin
data class MyConfig(
  @ConfigVersion val version: Int = 3,
  val welcome: String = "Welcome",
  val cooldownTicks: Int = 60
)

val config = Configs.load<MyConfig>(plugin) {
  migrations {
    from(1) { rename("message", "welcome") }
    from(2) {
      rename("cooldown", "cooldownTicks")
      transform("cooldownTicks") { (it as Int) * 20 }
      remove("legacy-flag")
    }
  }
}
```

Before changing a v1 file, DevCore writes `config.yml.bak-v1`. Migrations run consecutively and update the version only after each step succeeds. A missing step fails the load. If any operation throws, the original bytes are restored from the backup and the exception is propagated.
