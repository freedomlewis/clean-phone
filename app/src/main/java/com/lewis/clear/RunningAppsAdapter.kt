package com.lewis.clear

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lewis.clear.model.AppInfo
import kotlinx.android.synthetic.main.item_app_info.view.*


class RunningAppsAdapter(val context: Context, val appInfos: List<AppInfo>) : RecyclerView.Adapter<RunningAppsAdapter.AppsViewHolder>() {

    class AppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivLogo: ImageView = itemView.iv_logo
        var tvName: TextView = itemView.tv_app_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_app_info, parent, false)
        return AppsViewHolder(rootView)
    }

    override fun getItemCount(): Int = appInfos.size

    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        val appInfo = appInfos[position]
        holder.itemView.setOnClickListener {
            showPackageDetail(appInfo.packageName)
        }
        holder.ivLogo.setImageDrawable(appInfo.logo)
        holder.tvName.text = appInfo.name
    }

    private fun showPackageDetail(packageName: String) {
        ClearActivity.showPackageDetail(context, packageName)
    }

}
