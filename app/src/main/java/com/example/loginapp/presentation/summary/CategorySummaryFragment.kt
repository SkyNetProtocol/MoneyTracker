package com.example.loginapp.presentation.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.R
import com.example.loginapp.databinding.FragmentCategorySummaryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategorySummaryFragment : Fragment() {

    private var _binding: FragmentCategorySummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountSummaryViewModel by viewModels()
    private var userId: Int = -1
    private var category: String = "PERSONAL"

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_CATEGORY = "category"

        fun newInstance(userId: Int, category: String): CategorySummaryFragment {
            val fragment = CategorySummaryFragment()
            val args = Bundle()
            args.putInt(ARG_USER_ID, userId)
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(ARG_USER_ID)
            category = it.getString(ARG_CATEGORY, "PERSONAL")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategorySummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DateAdapter { date ->
            navigateToDateSummary(date)
        }
        binding.datesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.datesRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dates.collect { dates ->
                adapter.submitList(dates)
                binding.emptyStateTextView.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        if (userId != -1) {
            viewModel.loadDates(userId, category)
        }
    }

    private fun navigateToDateSummary(date: String) {
        val fragment = DateSummaryFragment.newInstance(userId, date, category)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class DateAdapter(private val onClick: (String) -> Unit) : androidx.recyclerview.widget.ListAdapter<String, DateAdapter.DateViewHolder>(DateDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_summary_date, parent, false)
            return DateViewHolder(view)
        }

        override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
            val date = getItem(position)
            holder.bind(date, onClick)
        }

        class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

            fun bind(date: String, onClick: (String) -> Unit) {
                dateTextView.text = date
                itemView.setOnClickListener { onClick(date) }
            }
        }

        class DateDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        }
    }
}
