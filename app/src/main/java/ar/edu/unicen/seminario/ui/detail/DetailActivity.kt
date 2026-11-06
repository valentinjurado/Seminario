package ar.edu.unicen.seminario.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ar.edu.unicen.seminario.R
import ar.edu.unicen.seminario.ServiceLocator
import ar.edu.unicen.seminario.data.remote.ApiClient
import ar.edu.unicen.seminario.data.remote.dto.MovieDetailDto
import ar.edu.unicen.seminario.databinding.ActivityDetailBinding
import ar.edu.unicen.seminario.ui.common.UiState
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val movieId: Int by lazy { intent.getIntExtra(EXTRA_MOVIE_ID, -1) }

    private val viewModel: DetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailViewModel(ServiceLocator.movieRepository, movieId) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.title = intent.getStringExtra(EXTRA_MOVIE_TITLE)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.retryButton.setOnClickListener { viewModel.load() }

        viewModel.state.observe(this) { render(it) }
    }

    private fun render(state: UiState<MovieDetailDto>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.errorGroup.isVisible = state is UiState.Error
        binding.scrollContent.isVisible = state is UiState.Success

        when (state) {
            is UiState.Success -> showMovie(state.data)
            is UiState.Error -> binding.errorMessage.text = getString(state.messageRes)
            else -> Unit
        }
    }

    private fun showMovie(movie: MovieDetailDto) {
        binding.toolbar.title = movie.title
        binding.title.text = movie.title
        binding.overview.text = movie.overview?.takeIf { it.isNotBlank() }
            ?: getString(R.string.no_overview)

        val year = movie.releaseDate?.take(4).orEmpty()
        val runtime = movie.runtime?.let { getString(R.string.runtime_format, it) }.orEmpty()
        binding.meta.text = listOf(year, runtime, getString(R.string.rating_format, movie.voteAverage))
            .filter { it.isNotBlank() }
            .joinToString("  ·  ")

        showGenres(movie)

        Glide.with(binding.poster)
            .load(movie.posterPath?.let { ApiClient.IMAGE_BASE_URL + it })
            .placeholder(R.drawable.ic_movie_placeholder)
            .error(R.drawable.ic_movie_placeholder)
            .centerCrop()
            .into(binding.poster)

        binding.shareButton.setOnClickListener { shareMovie(movie) }
        binding.homepageButton.isVisible = !movie.homepage.isNullOrBlank()
        binding.homepageButton.setOnClickListener { openHomepage(movie.homepage.orEmpty()) }
    }

    private fun showGenres(movie: MovieDetailDto) {
        binding.genresGroup.removeAllViews()
        movie.genres.orEmpty().forEach { genre ->
            val chip = Chip(this).apply { text = genre.name }
            binding.genresGroup.addView(chip)
        }
        binding.genresGroup.isVisible = !movie.genres.isNullOrEmpty()
    }

    private fun shareMovie(movie: MovieDetailDto) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, movie.title)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, movie.title, movie.overview.orEmpty()))
        }
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openHomepage(homepage: String) {
        val uri = homepage.toUri()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val EXTRA_MOVIE_ID = "extra_movie_id"
        private const val EXTRA_MOVIE_TITLE = "extra_movie_title"

        fun intent(context: Context, movieId: Int, title: String): Intent =
            Intent(context, DetailActivity::class.java)
                .putExtra(EXTRA_MOVIE_ID, movieId)
                .putExtra(EXTRA_MOVIE_TITLE, title)
    }
}
