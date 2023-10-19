package com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewSubmitOrderImagesBinding

class SubmitOrderImagesAdapter(
    private val context: Context
) : RecyclerView.Adapter<SubmitOrderImagesAdapter.ViewHolder>() {

    private var listOfImage: MutableList<Bitmap> = mutableListOf()
    private var onClickItem: ((Bitmap, Int, CardView) -> Unit)? = null
    private var onLongClickItem: ((Boolean) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemOfRecyclerViewSubmitOrderImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(image: Bitmap, position: Int) {
            with(binding) {
                Glide
                    .with(context)
                    .load(image)
                    .centerCrop()
                    .into(ivOrderImage)

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

    fun setOnItemClickListener(block: ((Bitmap, Int, CardView) -> Unit)) {
        onClickItem = block
    }

    fun setOnLongClickListener(block: ((Boolean) -> Unit)) {
        onLongClickItem = block
    }

    fun addImage(image: Bitmap, position: Int) {
        this.listOfImage.add(image)
        notifyItemInserted(position)
    }

    fun removeImage(position: Int) {
        this.listOfImage.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listOfImage.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewSubmitOrderImagesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listOfImage.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listOfImage[position], position)
    }
}