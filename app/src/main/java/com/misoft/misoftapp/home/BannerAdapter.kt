package com.misoft.misoftapp.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.misoft.misoftapp.R

class BannerAdapter(
    private val banners: List<Int>
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(
        val imagen: ImageView
    ) : RecyclerView.ViewHolder(imagen.parent as android.view.View)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BannerViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)

        val imagen = vista.findViewById<ImageView>(
            R.id.imgBanner
        )

        return BannerViewHolder(imagen)
    }

    override fun onBindViewHolder(
        holder: BannerViewHolder,
        position: Int
    ) {
        holder.imagen.setImageResource(banners[position])
    }

    override fun getItemCount(): Int = banners.size
}
