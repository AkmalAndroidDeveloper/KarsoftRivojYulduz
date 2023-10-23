package com.example.karsoftrivojyulduz.presentation.ui.histories.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewHistoryBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData

class HistoriesAdapter : RecyclerView.Adapter<HistoriesAdapter.ViewHolder>() {

    private var listOfHistory: List<OrderAndHistoryResponseData.Data>? = null
    private var onItemClick: ((OrderAndHistoryResponseData.Data, Int, String, String, String, String) -> Unit)? =
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

                root.setOnClickListener {
                    onItemClick?.invoke(
                        data,
                        data.id,
                        data.contact.title.toString(),
                        data.contact.address.toString(),
                        data.contact.name,
                        data.contact.phone
                    )
                }
            }
        }
    }

    fun setOnItemClickListener(block: ((OrderAndHistoryResponseData.Data, Int, String, String, String, String) -> Unit)) {
        onItemClick = block
    }

    fun submitList(listOfHistory: List<OrderAndHistoryResponseData.Data>) {
        this.listOfHistory = listOfHistory
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfHistory?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(
            listOfHistory?.get(position) ?: emptyList<OrderAndHistoryResponseData.Data>()[position]
        )
    }
}