package ar.edu.unicen.seminario.data.repository

import ar.edu.unicen.seminario.data.remote.TmdbApi
import ar.edu.unicen.seminario.data.remote.dto.MovieDetailDto
import ar.edu.unicen.seminario.data.remote.dto.MovieListResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepositoryImpl(private val api: TmdbApi) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): Result<MovieListResponse> =
        apiCall { api.getPopularMovies(page) }

    override suspend fun searchMovies(query: String, page: Int): Result<MovieListResponse> =
        apiCall { api.searchMovies(query, page) }

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetailDto> =
        apiCall { api.getMovieDetail(movieId) }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
