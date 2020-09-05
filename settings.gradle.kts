rootProject.name = "handy-gradle-kotlin-dsl-example"

include(":kotlin-example")
include(":spring-boot-application")

// 从阿里云仓库下载相关插件，提高速度
pluginManagement{
    repositories {
        maven{
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
    }
}