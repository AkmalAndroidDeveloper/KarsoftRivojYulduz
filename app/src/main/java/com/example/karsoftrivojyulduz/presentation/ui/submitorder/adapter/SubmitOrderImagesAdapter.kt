package com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewSubmitOrderImagesBinding
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData

class SubmitOrderImagesAdapter(
    private val context: Context
) : RecyclerView.Adapter<SubmitOrderImagesAdapter.ViewHolder>() {

    private var listOfImage: MutableList<SubmitImagesData> = mutableListOf()
    private var onClickItem: ((SubmitImagesData, Int, CardView) -> Unit)? = null
    private var onLongClickItem: ((Boolean) -> Unit)? = null

    companion object {
        private const val TAG = "SubmitOrderImagesAdapte"
    }

    inner class ViewHolder(private val binding: ItemOfRecyclerViewSubmitOrderImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(image: SubmitImagesData, position: Int) {
            with(binding) {
                Glide.with(context).load(image.image).centerCrop().into(ivOrderImage)

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

    fun setOnItemClickListener(block: ((SubmitImagesData, Int, CardView) -> Unit)) {
        onClickItem = block
    }

    fun setOnLongClickListener(block: ((Boolean) -> Unit)) {
        onLongClickItem = block
    }

    fun addImage(imageData: SubmitImagesData) {
        this.listOfImage.add(imageData)
        notifyItemInserted(this.listOfImage.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<SubmitImagesData>) {
        this.listOfImage = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemOfRecyclerViewSubmitOrderImagesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfImage.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listOfImage[position], position)
    }
}