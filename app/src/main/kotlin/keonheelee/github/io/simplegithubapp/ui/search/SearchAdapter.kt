package keonheelee.github.io.simplegithubapp.ui.search

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide

import java.util.ArrayList

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.ui.api.Model.GithubRepo
import kotlinx.android.synthetic.main.item_repository.view.*

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.RepositoryHolder>() {

    private var items: MutableList<GithubRepo> = mutableListOf()
    private val placeholder = ColorDrawable(Color.GRAY)
    private var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryHolder {
        return RepositoryHolder(parent)
    }

    override fun onBindViewHolder(holder: RepositoryHolder, position: Int) {
        items[position].let { repo ->
            with(holder.itemView) {
                Glide.with(holder.itemView.context)
                        .load(repo.owner.avartarUrl)
                        .placeholder(placeholder)
                        .into(holder.ivProfile)

                tvItemRepositoryName.text = repo.fullName
                tvItemRepositoryLanguage.text = if (TextUtils.isEmpty(repo.language))
                    context.getText(R.string.no_language_specified)
                else
                    repo.language

                setOnClickListener { listener?.onItemClick(repo) }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<GithubRepo>) {
        this.items = items.toMutableList()
    }

    fun setItemClickListener(listener: ItemClickListener?) {
        this.listener = listener
    }

    fun clearItems() {
        this.items.clear()
    }

    public class RepositoryHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repository, parent, false)) {
        var ivProfile: ImageView
        var tvName: TextView
        var tvLanguage: TextView

        init {
            ivProfile = itemView.findViewById(R.id.ivItemRepositoryProfile)
            tvName = itemView.findViewById(R.id.tvItemRepositoryName)
            tvLanguage = itemView.findViewById(R.id.tvItemRepositoryLanguage)
        }
    }

    interface ItemClickListener {
        fun onItemClick(repository: GithubRepo)
    }
}
