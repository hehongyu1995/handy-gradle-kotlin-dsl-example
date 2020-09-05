# handy-gradle-kotlin-dsl-example

这是一个示例项目，向你分享我如何使用Gradle Kotlin DSL构建多模块项目。包括编译Spring Boot FatJar、混合编译Java+Kotlin项目。
以及如何更好的组织构建代码。

## 构建项目

```bash
gradlew package
```

## 简要说明

所有的构建逻辑都在根项目里便携，使用Kotlin扩展函数提高可读性。自模块中只需要声明所需依赖即可。

例如，定义以下针对Project的扩展函数：

```kotlin
fun Project.packageBootJar(){
    apply(plugin = "org.springframework.boot")
    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>{
        archiveFileName.set("app.jar")
    }
}
```

如果需要项目可以编译为可执行的Spring Boot FatJar，仅需要声明：

```kotlin
project(":spring-boot-application").packageBootJar()
```

如果需要使项目可以编译Kotlin代码，仅需要声明：

```kotlin
project(":spring-boot-application").compileKotlin()
```

详细逻辑，请参考[build.gradle.kts](build.gradle.kts)