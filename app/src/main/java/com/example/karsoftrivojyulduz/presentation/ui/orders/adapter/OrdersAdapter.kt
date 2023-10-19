package com.example.karsoftrivojyulduz.presentation.ui.orders.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewOrdersBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.Data
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData

class OrdersAdapter : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    private var listOfOrder: List<OrderAndHistoryResponseData.Data>? = null
    private var onItemClick: ((OrderAndHistoryResponseData.Data, Int) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemOfRecyclerViewOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(data: OrderAndHistoryResponseData.Data) {
            with(binding) {
                tvOrderTitle.text = data.contact.title.toString()
                tvOrderId.text = "№${data.id}"
                tvOrderLocation.text = data.contact.address.toString()
                tvCustomerPhoneNumber.text = data.contact.phone

                root.setOnClickListener {
                    onItemClick?.invoke(data, data.id)
                }
            }
        }
    }

    fun setOnItemClickListener(block: ((OrderAndHistoryResponseData.Data, Int) -> Unit)) {
        onItemClick = block
    }

    fun submitList(listOfOrder: List<OrderAndHistoryResponseData.Data>) {
        this.listOfOrder = listOfOrder
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewOrdersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfOrder?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(
            listOfOrder?.get(position) ?: emptyList<OrderAndHistoryResponseData.Data>()[position]
        )
    }
}