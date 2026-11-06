package ar.edu.unicen.seminario.ui.common

import ar.edu.unicen.seminario.R
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException

class ErrorMessagesTest {

    @Test
    fun `los mensajes de error son distintos entre si`() {
        assertNotEquals(R.string.error_no_internet, R.string.error_token)
        assertNotEquals(R.string.error_no_internet, R.string.error_server)
        assertNotEquals(R.string.error_server, R.string.error_unknown)
    }

    @Test
    fun `un error de red se mapea a sin conexion`() {
        assertEquals(R.string.error_no_internet, UnknownHostException().toUserMessage())
        assertEquals(R.string.error_no_internet, IOException().toUserMessage())
    }

    @Test
    fun `un 401 se mapea a token invalido`() {
        assertEquals(R.string.error_token, httpError(401).toUserMessage())
    }

    @Test
    fun `un 500 se mapea a error del servidor`() {
        assertEquals(R.string.error_server, httpError(500).toUserMessage())
    }

    @Test
    fun `un error desconocido se mapea al mensaje generico`() {
        assertEquals(R.string.error_unknown, IllegalStateException().toUserMessage())
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))
}
