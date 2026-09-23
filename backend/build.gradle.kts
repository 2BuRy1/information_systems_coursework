plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("java")
    id("checkstyle")
}

group = "ru.itmo"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

checkstyle {
    toolVersion = "10.21.0"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

val googleJavaFormat by configurations.creating

dependencies {
    val lombokVersion = "1.18.36"

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    googleJavaFormat("com.google.googlejavaformat:google-java-format:1.24.0")
}

tasks.test {
    useJUnitPlatform()
}

fun javaSourceFiles(): List<String> =
    fileTree("src/main/java") { include("**/*.java") }.files.map { it.absolutePath }.sorted()

tasks.register<JavaExec>("formatJava") {
    group = "formatting"
    description = "Formats Java sources using google-java-format."
    classpath = googleJavaFormat
    mainClass.set("com.google.googlejavaformat.java.Main")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
    jvmArgs = listOf(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    )
    args = listOf("--replace") + javaSourceFiles()
    onlyIf { !gradle.startParameter.isOffline }
}

tasks.register<JavaExec>("checkJavaFormat") {
    group = "verification"
    description = "Checks Java formatting using google-java-format."
    classpath = googleJavaFormat
    mainClass.set("com.google.googlejavaformat.java.Main")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
    jvmArgs = listOf(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    )
    args = listOf("--dry-run", "--set-exit-if-changed") + javaSourceFiles()
    onlyIf { !gradle.startParameter.isOffline }
}

tasks.named("check") {
    dependsOn("checkJavaFormat")
}
