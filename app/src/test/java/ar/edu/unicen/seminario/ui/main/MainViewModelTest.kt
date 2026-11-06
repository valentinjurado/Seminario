package ar.edu.unicen.seminario.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ar.edu.unicen.seminario.MainDispatcherRule
import ar.edu.unicen.seminario.R
import ar.edu.unicen.seminario.data.remote.dto.MovieDetailDto
import ar.edu.unicen.seminario.data.remote.dto.MovieDto
import ar.edu.unicen.seminario.data.remote.dto.MovieListResponse
import ar.edu.unicen.seminario.data.repository.MovieRepository
import ar.edu.unicen.seminario.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()

    @Test
    fun `al iniciar muestra las peliculas populares`() = runTest(mainDispatcherRule.scheduler) {
        repository.popularResult = Result.success(response(movie(1), movie(2)))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
    }

    @Test
    fun `sin internet muestra el mensaje de conexion`() = runTest(mainDispatcherRule.scheduler) {
        repository.popularResult = Result.failure(IOException("sin conexion"))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        assertEquals(UiState.Error(R.string.error_no_internet), viewModel.state.value)
    }

    @Test
    fun `con token invalido muestra el mensaje de configuracion`() = runTest(mainDispatcherRule.scheduler) {
        repository.popularResult = Result.failure(HttpException(Response.error<Any>(401, "".toResponseBody())))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        assertEquals(UiState.Error(R.string.error_token), viewModel.state.value)
    }

    @Test
    fun `una busqueda sin resultados queda en vacio`() = runTest(mainDispatcherRule.scheduler) {
        repository.searchResult = Result.success(response())

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        viewModel.onQueryChanged("algoquenoexiste")
        advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.state.value)
        assertEquals("algoquenoexiste", repository.lastQuery)
    }

    @Test
    fun `al borrar la busqueda vuelve a mostrar las populares`() = runTest(mainDispatcherRule.scheduler) {
        repository.searchResult = Result.success(response(movie(9)))
        repository.popularResult = Result.success(response(movie(1), movie(2)))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        viewModel.onQueryChanged("batman")
        advanceUntilIdle()
        viewModel.onQueryChanged("")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
        assertEquals("", viewModel.query)
    }

    private fun response(vararg movies: MovieDto) =
        MovieListResponse(page = 1, results = movies.toList(), totalPages = 1)

    private fun movie(id: Int) =
        MovieDto(id, "Pelicula $id", posterPath = null, "Sinopsis", 7.5, "2024-01-01")
}

class FakeMovieRepository : MovieRepository {

    var popularResult: Result<MovieListResponse> = Result.success(MovieListResponse(1, emptyList(), 1))
    var searchResult: Result<MovieListResponse> = Result.success(MovieListResponse(1, emptyList(), 1))
    var lastQuery: String? = null

    override suspend fun getPopularMovies(page: Int): Result<MovieListResponse> = popularResult

    override suspend fun searchMovies(query: String, page: Int): Result<MovieListResponse> {
        lastQuery = query
        return searchResult
    }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetailDto> =
        Result.failure(UnsupportedOperationException())
}
