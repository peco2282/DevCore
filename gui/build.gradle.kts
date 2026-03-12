version = properties["devcore.gui.version"] ?:"1.0.0"

dependencies {
  compileOnly(libs.paper.api)
  testImplementation(libs.paper.api)
  testImplementation(libs.kotlin.test)
}
