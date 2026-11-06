package ar.edu.unicen.seminario.data.repository

import ar.edu.unicen.seminario.data.remote.dto.MovieDetailDto
import ar.edu.unicen.seminario.data.remote.dto.MovieListResponse

interface MovieRepository {

    suspend fun getPopularMovies(page: Int = 1): Result<MovieListResponse>

    suspend fun searchMovies(query: String, page: Int = 1): Result<MovieListResponse>

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetailDto>
}
