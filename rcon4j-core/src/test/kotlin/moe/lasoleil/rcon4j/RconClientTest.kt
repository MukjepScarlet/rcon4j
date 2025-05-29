package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.testing.EmbeddedRconServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.io.IOException
import java.net.InetAddress
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RconClientTest {

    companion object {
        private lateinit var server: EmbeddedRconServer

        @AfterAll
        @JvmStatic
        fun shutdownServer() {
            server.stop()
        }

        @BeforeAll
        @JvmStatic
        fun startServer() {
            server = EmbeddedRconServer(password = "vanilla", port = 0)
            server.start()
        }
    }

    private inline fun useClient(block: (client: RconClient) -> Unit) {
        RconClient.createDefault().use(block)
    }

    @Test
    fun testSuccessfulAuthentication() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)

        val id = Random.nextInt(1, 10000)

        val isAuthenticated = client.authenticate("vanilla", id)

        assertTrue(isAuthenticated)
        assertEquals(server.lastAuthId, id)
    }

    @Test
    fun testFailedAuthentication() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)
        val isAuthenticated = client.authenticate("azuki")

        assertFalse(isAuthenticated)
        assertEquals(-1, server.lastAuthResponseId)
    }

    @Test
    fun testCommandExecutionAfterAuth() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)
        client.authenticate("vanilla")

        val response = client.command("ping")
        assertEquals("pong", response)

        assertEquals("ping", server.lastCommand)
    }

    @Test
    fun testUnauthenticatedCommand() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)

        val response = client.command("ping")

        assertEquals("unauthorized", response)
    }

    @Test
    fun testCustomCommandHandling() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)
        client.authenticate("vanilla")

        val response = client.command("version")
        assertEquals(EmbeddedRconServer.VERSION, response)
    }

    @Test
    fun testEchoCommand() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)
        client.authenticate("vanilla")

        val testString = "echo hello!"
        val response = client.command(testString)
        assertEquals(testString.substringAfter("echo "), response)
    }

    @Test
    fun testAuthenticationWithCustomId() = useClient { client ->
        val customId = Random.nextInt(100, 1900)
        client.connect(InetAddress.getByName("localhost"), server.port)
        val isAuthenticated = client.authenticate("vanilla", customId)

        assertTrue(isAuthenticated)
        assertEquals(customId, server.lastAuthId)
    }

    @Test
    fun testAuthenticationWithInvalidId() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)

        assertFailsWith<IllegalArgumentException> {
            client.authenticate("vanilla", -1)
        }
    }

    @Test
    fun testClientClose() = useClient { client ->
        client.connect(InetAddress.getByName("localhost"), server.port)
        client.close()

        assertFailsWith<IOException> {
            client.command("ping")
        }
    }

    @Test
    fun testServerExceptionHandling() = useClient { client ->
        server.shouldThrowException = true

        client.connect(InetAddress.getByName("localhost"), server.port)
        client.authenticate("vanilla")

        assertTrue(client.command("invalid_command").startsWith("Internal server error"))

        assertTrue(server.exceptionOccurred)
    }

}
