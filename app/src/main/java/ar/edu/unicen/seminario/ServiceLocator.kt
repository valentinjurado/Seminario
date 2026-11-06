package ar.edu.unicen.seminario

import ar.edu.unicen.seminario.data.remote.ApiClient
import ar.edu.unicen.seminario.data.repository.MovieRepository
import ar.edu.unicen.seminario.data.repository.MovieRepositoryImpl

object ServiceLocator {
    val movieRepository: MovieRepository by lazy { MovieRepositoryImpl(ApiClient.api) }
}
