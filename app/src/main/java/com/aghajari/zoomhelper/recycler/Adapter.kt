package com.aghajari.zoomhelper.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.zoomhelper.R
import com.aghajari.zoomhelper.ZoomHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class Adapter(private val data: List<Model>) : RecyclerView.Adapter<Adapter.ViewHolder>(),
    ZoomHelper.OnZoomStateChangedListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView =
            LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: AppCompatImageView = itemView.findViewById(R.id.avatar)
        val image: AppCompatImageView = itemView.findViewById(R.id.image)
        val name: AppCompatTextView = itemView.findViewById(R.id.name)
        val text: AppCompatTextView = itemView.findViewById(R.id.text)

        fun bind(model: Model) {
            image.layoutParams.height = model.height
            name.text = model.name
            text.text = model.text
            ZoomHelper.addZoomableView(image, model)

            Glide.with(itemView)
                .load(model.avatar)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.place_holder))
                .into(avatar)

            Glide.with(itemView)
                .load(model.image)
                .apply(RequestOptions.centerCropTransform().placeholder(R.drawable.place_holder))
                .into(image)
        }

    }

    override fun onZoomStateChanged(
        zoomHelper: ZoomHelper,
        zoomableView: View,
        isZooming: Boolean
    ) {
        if (isZooming) {
            val model = ZoomHelper.getZoomableViewTag(zoomableView) as Model
            Toast.makeText(
                zoomableView.context,
                model.name + "'s post started zooming!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}