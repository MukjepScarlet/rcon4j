package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.testing.startNettyRconServer
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RconClientTest {

    private lateinit var serverThread: Thread
    private var serverPort: Int = 0

    @AfterTest
    fun shutdownServer() {
        serverThread.interrupt()
    }

    @BeforeTest
    fun startServer() {
        serverPort = Random.nextInt(30000, 40000)
        serverThread = thread {
            startNettyRconServer(password = "vanilla", port = serverPort)
        }
    }

    @Test
    fun testAuth() {
        RconClient.createDefault().use {
            it.connect("localhost", serverPort)
            assertTrue(it.authenticate("vanilla"))
            assertFalse(it.authenticate("azuki"))
        }
    }

}
