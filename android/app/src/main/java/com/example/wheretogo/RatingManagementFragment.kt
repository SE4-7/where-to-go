package com.example.wheretogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RatingManagementFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var viewAdapter: ViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rating_management, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter()
        recyclerView = view.findViewById(R.id.recyclerview_rating)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return view
    }

    inner class ViewAdapter : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
        private val items = ArrayList<PlaceInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun setItem(item: PlaceInfo) {

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAdapter.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.list_item, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewAdapter.ViewHolder, position: Int) {
            val item = items[position]
            holder.setItem(item)
        }

        override fun getItemCount(): Int = items.size
    }
}