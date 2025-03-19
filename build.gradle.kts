plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/poseidon.jar"))
    implementation(files("libs/PermissionsEx.jar"))
    implementation(files("libs/dbc.jar"))
    implementation(files("libs/Essentials.jar"))
    implementation("org.apache.commons:commons-dbcp2:2.13.0")
}