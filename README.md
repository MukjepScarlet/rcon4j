# rcon4j [![](https://jitpack.io/v/MukjepScarlet/rcon4j.svg)](https://jitpack.io/#MukjepScarlet/rcon4j)

An RCON client library for Java and Kotlin.

## Modules

- `rcon4j-core`: in pure Java 8. Connect with Socket.
- `rcon4j-kotlin`: in Kotlin. Connect with [ktor-network-jvm](https://github.com/ktorio/ktor/tree/main/ktor-network/jvm) library and Kotlin coroutines.

## Usage

1. Include the JitPack repository to your project, example (`build.gradle.kts`):
    ```kotlin
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
    ```
2. Import the library with a proper version:
    ```kotlin
    dependencies {
        implementation("com.github.MukjepScarlet.rcon4j:rcon4j-kotlin:$version")
        implementation("com.github.MukjepScarlet.rcon4j:rcon4j-core:$version")
    }
    ```

## Overview

### Java module
```java
public RconClient(
    String host,
    int port,
    String password,
    Charset charset, // default=StandardCharsets.UTF_8
    int requestId // default=(new Random()).nextInt(Integer.MAX_VALUE) + 1
) throws IOException, AuthenticationException

// RconClient implements AutoCloseable

// Member
@NotNull   
public String command(
    String payload
)
throws IOException
```

### Kotlin module
```kotlin
suspend fun ARconClient(
    host: String,
    port: Int,
    password: String,
    charset: Charset = Charsets.UTF_8,
    requestId: Int = Random.nextInt(0, Int.MAX_VALUE) + 1,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ARconClient

// ARconClient implements AutoCloseable

// Member
public final suspend fun command(
    payload: String
): String
```
