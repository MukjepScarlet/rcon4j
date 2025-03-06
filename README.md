# rcon4j [![](https://jitpack.io/v/MukjepScarlet/rcon4j.svg)](https://jitpack.io/#MukjepScarlet/rcon4j)

An RCON client library for Java and Kotlin (JVM/Android).

Depends on: [kotlin-stdlib](https://kotlinlang.org/)

## Modules

- `rcon4j-core`: Based on Java Socket API.
- `rcon4j-kotlin`: Based on [ktor-network](https://github.com/ktorio/ktor/tree/main/ktor-network/) (CIO) library and Kotlin coroutines.

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
        implementation("com.github.MukjepScarlet.rcon4j:rcon4j-core:$version")
        implementation("com.github.MukjepScarlet.rcon4j:rcon4j-kotlin:$version")
    }
    ```

## Overview

Default Charset: `StandardCharsets.UTF_8`

Default request ID: `Random.nextInt(1, Int.MAX_VALUE)` (Kotlin thread-safe Random)

### Socket

```java
// Packet
class RconPacket(int id, int type, byte[] payload) { /*...*/ }

class RconClient implements AutoCloseable {
   public RconClient(
           String host,
           int port,
           String password,
           Charset charset, // default
           int requestId // default
   ) throws IOException, AuthenticationException { /*...*/ }

   @NotNull
   public String command(@NotNull String payload) throws IOException { /*...*/ }
}
```

### CIO

```kotlin
suspend fun ARconClient(
   host: String,
   port: Int,
   password: String,
   charset: Charset, // default
   requestId: Int, // default
   socketBuilder: SocketBuilder,
): ARconClient { /*...*/ }

suspend fun ARconClient.command(payload: String): String { /*...*/ }
```
