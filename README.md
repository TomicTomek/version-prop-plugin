# In order to import that plugin:
In file: `<PROJECT_ROOT>/settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        mavenLocal()  // for local testing
        maven {
           name = "GitHubPackages"
           url = uri("https://maven.pkg.github.com/TomicTomek/version-prop-plugin")
        }
    }
}
```


In file: `<PROJECT_ROOT>/app/build.gradle.kts`
```kotlin
plugins {
    id("jsp.version-prop") version "1.0.0"
}
```


# Usage:
Create file that will hold version properties i.e.: `<PROJECT_ROOT>/version.properties` with the following content:
```properties
version.code=2

version.major=1
version.minor=2
version.patch=3
```

In file: `<PROJECT_ROOT>/app/build.gradle.kts`
```kotlin
versionPropDef {
    propertyFile = rootProject.layout.projectDirectory.file("version.properties")
    versionNameSuffix = System.getenv("BUILD_RUN_NUMBER")
}

 ...

android {
    defaultConfig {
      
        ...
          
        versionCode = versionPropDef.version.code
        versionName = versionPropDef.version.name
    }
}
```
# Additional task
`./gradlew printVersionFileContent`

# Publication
#### Add repository
In the project root folder open `<PROJECT ROOT>/settings.gradle` and in `dependencyResolutionManagement.repositories` add following repository
```
maven {  
    name = "GitHubPackages"  
    url = uri("https://maven.pkg.github.com/TomicTomek/version-prop-plugin")  
    credentials {  
        /** Create github.properties in root project folder file with  
         * gpr.usr=GITHUB_USER_ID & gpr.key=PERSONAL_ACCESS_TOKEN         
         * Set env variable GPR_USER & GPR_API_KEY if not adding a properties file         
         * Personal Access Token (classic) has to have packages:read permissions         
         */
        val githubProperties = java.util.Properties().apply {
            File(rootProject.projectDir, "github.properties").takeIf(File::exists)?.let { githubPropertiesFile ->
                load(java.io.FileInputStream(githubPropertiesFile.absolutePath))
            }
        }
        username = "${githubProperties["gpr.usr"] ?: System.getenv("GPR_USER")}"  
        password = "${githubProperties["gpr.key"] ?: System.getenv("GPR_API_KEY")}"  
    }  
}
```

##### Credentials can be provided in the two ways:
1. As a environment variables:
```
export PERSONAL_ACCESS_TOKEN=[Personal Access Token (classic) that has packages:write permissions on https://github.com/spectrio/android-proofOfPlay repository]
export GPR_USER=[GitHub user name related to PERSONAL_ACCESS_TOKEN]
```

2. In the project root create file `<PROJECT ROOT>/github.properties` wit the following content:
```
gpr.usr=[GitHub user name related to PERSONAL_ACCESS_TOKEN]
gpr.key=[Personal Access Token (classic) that has packages:write permissions on https://github.com/spectrio/android-proofOfPlay repository]
```