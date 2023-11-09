package com.example.karsoftrivojyulduz.presentation.ui.orders.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewOrdersBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.util.constant.Constants

class OrdersAdapter(
) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    private var listOfOrder: List<OrderAndHistoryResponseData.Data> = listOf()
    private var onItemClick: ((OrderAndHistoryResponseData.Data) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemOfRecyclerViewOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(data: OrderAndHistoryResponseData.Data) {
            with(binding) {
                tvOrderTitle.text = (data.contact.title ?: "Не указан").toString()
                tvOrderId.text = "№${data.id}"
                tvOrderLocation.text = (data.contact.address ?: "Не указан").toString()
                tvCustomerPhoneNumber.text = data.contact.phone

                when (data.statusId) {
                    Constants.METER_ATTACHED -> {
                        tvStatus.text = tvStatus.context.getString(R.string.new_order)
                    }

                    Constants.REDO_THE_WORK -> {
                        tvStatus.text = tvStatus.context.getString(R.string.remake_work)
                    }
                }

                root.setOnClickListener {
                    onItemClick?.invoke(
                        data
                    )
                }
            }
        }
    }

    fun setOnItemClickListener(block: ((OrderAndHistoryResponseData.Data) -> Unit)) {
        onItemClick = block
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(listOfOrder: List<OrderAndHistoryResponseData.Data>) {
        this.listOfOrder = listOfOrder
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewOrdersBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfOrder.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(
            listOfOrder[position]
        )
    }
}