package ar.edu.unicen.seminario.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.seminario.R
import ar.edu.unicen.seminario.data.remote.ApiClient
import ar.edu.unicen.seminario.data.remote.dto.MovieDto
import ar.edu.unicen.seminario.databinding.ItemMovieBinding
import com.bumptech.glide.Glide

class MovieAdapter(
    private val onMovieClick: (MovieDto) -> Unit
) : ListAdapter<MovieDto, MovieAdapter.MovieViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieDto) {
            binding.title.text = movie.title
            binding.rating.text = binding.root.context.getString(R.string.rating_format, movie.voteAverage)
            binding.year.text = movie.releaseDate?.take(4).orEmpty()

            Glide.with(binding.poster)
                .load(movie.posterPath?.let { ApiClient.IMAGE_BASE_URL + it })
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .centerCrop()
                .into(binding.poster)

            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MovieDto>() {
            override fun areItemsTheSame(oldItem: MovieDto, newItem: MovieDto) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MovieDto, newItem: MovieDto) = oldItem == newItem
        }
    }
}
