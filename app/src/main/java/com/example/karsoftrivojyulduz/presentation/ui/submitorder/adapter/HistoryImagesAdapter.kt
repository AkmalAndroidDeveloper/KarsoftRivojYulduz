package com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewSubmitOrderImagesBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.Image

class HistoryImagesAdapter(
    private val context: Context
) : RecyclerView.Adapter<HistoryImagesAdapter.ViewHolder>() {

    private var listOfImage: List<Image> = listOf()
    private var onClickItem: ((Image, Int, CardView) -> Unit)? = null
    private var onLongClickItem: ((Boolean) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemOfRecyclerViewSubmitOrderImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(image: Image, position: Int) {
            with(binding) {
                Glide.with(context).load(image.image_url).centerCrop().into(ivOrderImage)

                root.setOnClickListener {
                    onClickItem?.invoke(image, position, cardSelect)
                }
                root.setOnLongClickListener {
                    onLongClickItem?.invoke(true)
                    true
                }
            }
        }
    }

    fun setOnItemClickListener(block: ((Image, Int, CardView) -> Unit)) {
        onClickItem = block
    }

    fun setOnLongClickListener(block: ((Boolean) -> Unit)) {
        onLongClickItem = block
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sumbitList(listOfImage: List<Image>) {
        this.listOfImage = listOfImage
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewSubmitOrderImagesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listOfImage.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listOfImage[position], position)
    }
}