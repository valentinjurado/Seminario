package ar.edu.unicen.seminario.ui.main

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import ar.edu.unicen.seminario.R
import ar.edu.unicen.seminario.ServiceLocator
import ar.edu.unicen.seminario.data.remote.dto.MovieDto
import ar.edu.unicen.seminario.databinding.ActivityMainBinding
import ar.edu.unicen.seminario.ui.common.UiState
import ar.edu.unicen.seminario.ui.detail.DetailActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MovieAdapter

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(ServiceLocator.movieRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupList()
        setupSearch()

        binding.retryButton.setOnClickListener { viewModel.retry() }

        viewModel.state.observe(this) { render(it) }
    }

    private fun setupList() {
        adapter = MovieAdapter { movie -> openDetail(movie) }
        binding.moviesList.layoutManager = if (isLandscape()) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
        binding.moviesList.adapter = adapter
        binding.moviesList.setHasFixedSize(true)
    }

    private fun setupSearch() {
        binding.searchEdit.setText(viewModel.query)
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s?.toString().orEmpty())
            }
        })
    }

    private fun render(state: UiState<List<MovieDto>>) {
        binding.progressBar.isVisible = state is UiState.Loading
        binding.errorGroup.isVisible = state is UiState.Error
        binding.emptyMessage.isVisible = state is UiState.Empty
        binding.moviesList.isVisible = state is UiState.Success

        when (state) {
            is UiState.Success -> adapter.submitList(state.data)
            is UiState.Error -> binding.errorMessage.text = getString(state.messageRes)
            else -> Unit
        }
    }

    private fun openDetail(movie: MovieDto) {
        startActivity(DetailActivity.intent(this, movie.id, movie.title))
    }

    private fun isLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
