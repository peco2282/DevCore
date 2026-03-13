repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
}
version = properties["devcore.packet.version"] ?: "1.0.0"

dependencies {
    implementation(libs.kotlin.stdlib)
    compileOnly(libs.paper.api)
    testImplementation(libs.kotlin.test)
    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
}

