import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    `maven-publish`
    `java-library`
    // 插件版本定义
    kotlin("plugin.jpa") version "1.4.0" apply false
    kotlin("jvm") version "1.4.0" apply false
    kotlin("plugin.spring") version "1.4.0" apply false
    // Spring Boot版本
    id("org.springframework.boot") version "2.0.8.RELEASE" apply false
}

object Versions {
    const val springCloud = "Finchley.RELEASE"
    const val lombok = "1.18.12"
    const val kotlin = "1.4.0"
    const val groovy = "2.5.11"
    const val vavr = "0.9.0"
}

/**
 * 私服配置，填入自己的Nexus私服地址
 */
object NexusPublishConfig{
    const val url = "http://192.168.0.1:8099"
    const val username = "admin"
    const val password = "admin"
}

allprojects {
    group = "cn.hongyu"
    version = "0.0.1"
}

// 对子项目应用通用配置
subprojects {
    applyDependencyReposConfig()
    applyJavaCompilerConfig()
    applyJavaCommonDependencies()
    applyMavenPublishRepoConfig()
    applyJarMavenPublicationConfig()
}

// 配置Spring Boot应用
project(":spring-boot-application").packageBootJar()

// 配置包含Kotlin源码的项目
project(":spring-boot-application").compileKotlin()
project(":kotlin-example").compileKotlin()

/**
 * 应用Java编译器配置
 */
fun Project.applyJavaCompilerConfig() {
    if (hasSubProjects() || isBomProject()) {
        logger.debug("Skip applying java compiler config for project $name")
        return
    }
    apply(plugin = "java")
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // 打包源码
        withSourcesJar()
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

/**
 * 应用通用的Java依赖和依赖管理
 */
fun Project.applyJavaCommonDependencies() {
    if (hasSubProjects() || isBomProject()) return
    apply(plugin = "java-library")
    dependencies {
        api("io.vavr:vavr:${Versions.vavr}")
        implementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
        implementation(enforcedPlatform("org.springframework.cloud:spring-cloud-dependencies:${Versions.springCloud}"))
        compileOnly("org.projectlombok:lombok:${Versions.lombok}")
        annotationProcessor("org.projectlombok:lombok:${Versions.lombok}")
        testCompileOnly("org.projectlombok:lombok:${Versions.lombok}")
        testAnnotationProcessor("org.projectlombok:lombok:${Versions.lombok}")
    }
    configurations.all {
        // 对所有依赖排除logging日志传递依赖
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlin") useVersion(Versions.kotlin)
                if (requested.group == "org.codehaus.groovy") useVersion(Versions.groovy)
            }
        }
    }
}

/**
 * 应用Maven发布配置
 */
fun Project.applyMavenPublishRepoConfig() {
    if (isSpringBootProject()) return
    apply(plugin = "maven-publish")
    publishing {
        repositories {
            maven {
                val privateNexus = NexusPublishConfig.url
                val releasesRepoUrl = "${privateNexus}/repository/maven-releases/"
                val snapshotsRepoUrl = "${privateNexus}/repository/maven-snapshots/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = NexusPublishConfig.username
                    password = NexusPublishConfig.password
                }
            }
        }
    }
}

/**
 * 发布Jar包至Maven
 */
fun Project.applyJarMavenPublicationConfig() {
    if (isSpringBootProject() || isBomProject()) return
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    publishing {
        repositories {
            publications {
                create<MavenPublication>("jar") {
                    groupId = group.toString()
                    artifactId = project.name
                    version = project.version.toString()

                    from(components["java"])
                }
            }
        }
    }
}

/**
 * 应用仓库配置
 */
fun Project.applyDependencyReposConfig() {
    repositories {
        // 优先查找本地仓库
        // 如果遇到某模块下载不全导致依赖无法解析，请删除本地Maven仓库中的该模块，或将该模块从本地仓库排除
        // 参考：https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:case-for-maven-local
        mavenLocal{
            content {
                // WeCross依赖该模块的classifier:linux-x86_64版本，从本地仓库查找可能找不到这个版本的依赖
                excludeModule("io.netty","netty-transport-native-epoll")
            }
        }
        // 私服配置
//        maven {
//            url = uri("${NexusPublishConfig.url}/repository/maven-public/")
//        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
    }
}

/**
 * 让项目可以编译Kotlin
 */
fun Project.compileKotlin(){
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    sourceSets.main {
        java.srcDirs("src/main/java", "src/main/kotlin")
    }
    val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict","-Xjvm-default=enable")
        jvmTarget = "1.8"
        languageVersion = "1.4"
    }
}

/**
 * 将项目打包为可以直接运行的BootJar
 */
fun Project.packageBootJar(){
    apply(plugin = "org.springframework.boot")
    tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>{
        archiveFileName.set("app.jar")
    }
}

fun Project.hasSubProjects() = subprojects.isNotEmpty()
fun Project.isBomProject() = name.endsWith("bom")
fun Project.isSpringBootProject() = name.endsWith("api") || name.endsWith("service")