package com.example.karsoftrivojyulduz.presentation.ui.histories.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewHistoryBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.util.constant.Constants

class HistoriesAdapter(
    private val context: Context
) : RecyclerView.Adapter<HistoriesAdapter.ViewHolder>() {

    private var listOfHistory: List<OrderAndHistoryResponseData.Data> = listOf()
    private var onItemClick: ((OrderAndHistoryResponseData.Data) -> Unit)? =
        null

    inner class ViewHolder(private val binding: ItemOfRecyclerViewHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(data: OrderAndHistoryResponseData.Data) {
            with(binding) {
                tvOrderTitle.text = data.contact.title.toString()
                tvOrderId.text = "№${data.id}"
                tvOrderLocation.text = data.contact.address.toString()
                tvCustomerPhoneNumber.text = data.contact.phone
                tvOrderCreatedDate.text = formatDate(data.createdAt)
                tvOrderSubmittedDate.text = formatDate(data.submittedAt)

                if (data.statusId == Constants.METER_HAS_COMPLETED_ITS_WORK)
                    tvStatus.text = context.getString(R.string.successfully_finished)

                root.setOnClickListener {
                    onItemClick?.invoke(data)
                }
            }
        }

        private fun formatDate(date: String): String {
            return date.substring(0, date.lastIndexOf(':')).replace(" ", ", ")
        }
    }

    fun setOnItemClickListener(block: ((OrderAndHistoryResponseData.Data) -> Unit)) {
        onItemClick = block
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(listOfHistory: List<OrderAndHistoryResponseData.Data>) {
        this.listOfHistory = listOfHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfHistory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(
            listOfHistory[position]
        )
    }
}