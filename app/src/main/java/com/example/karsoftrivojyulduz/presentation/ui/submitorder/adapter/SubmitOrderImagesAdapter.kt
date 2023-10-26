package com.example.karsoftrivojyulduz.presentation.ui.submitorder.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.karsoftrivojyulduz.databinding.ItemOfRecyclerViewSubmitOrderImagesBinding
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitImagesData
import com.example.karsoftrivojyulduz.domain.model.submitorder.SubmitOrderImagesData

class SubmitOrderImagesAdapter(
    private val context: Context
) : RecyclerView.Adapter<SubmitOrderImagesAdapter.ViewHolder>() {

    private var listOfImage: MutableList<SubmitImagesData> = mutableListOf()
    private var onClickItem: ((SubmitImagesData, Int, CardView) -> Unit)? = null
    private var onLongClickItem: ((SubmitImagesData, Boolean, Int, CardView) -> Unit)? = null

    companion object {
        private const val TAG = "SubmitOrderImagesAdapte"
    }

    inner class ViewHolder(private val binding: ItemOfRecyclerViewSubmitOrderImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(image: SubmitImagesData, position: Int) {
            with(binding) {
                Glide
                    .with(context)
                    .load(image.uri)
                    .centerCrop()
                    .into(ivOrderImage)
                cardSelect.visibility = View.INVISIBLE

                root.setOnClickListener {
                    onClickItem?.invoke(image, position, cardSelect)
                }
                root.setOnLongClickListener {
                    onLongClickItem?.invoke(image, true, position, cardSelect)
                    true
                }
            }
        }

        private fun getBitmapFromUri(uri: Uri): Bitmap {
            return MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )
        }
    }

    fun setOnItemClickListener(block: ((SubmitImagesData, Int, CardView) -> Unit)) {
        onClickItem = block
    }

    fun setOnLongClickListener(block: ((SubmitImagesData, Boolean, Int, CardView) -> Unit)) {
        onLongClickItem = block
    }

    fun addImage(imageData: SubmitImagesData) {
        this.listOfImage.add(imageData)
        notifyItemInserted(this.listOfImage.size - 1)
    }

    fun deleteImage(listOfItems: List<SubmitImagesData>) {
        listOfItems.forEach {
            this.listOfImage.remove(it)
            notifyItemRemoved(it.id - 1)
            notifyItemRangeChanged(it.id - 1, listOfItems.size)
        }
    }

    fun submitList(list: List<SubmitImagesData>) {
        this.listOfImage = list.toMutableList()
        notifyDataSetChanged()
    }

    fun clearList() {
        this.listOfImage.clear()
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