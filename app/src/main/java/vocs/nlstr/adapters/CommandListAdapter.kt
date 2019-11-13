package vocs.nlstr.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_list_main.view.*
import vocs.nlstr.R
import vocs.nlstr.models.CommandListItemData

class CommandListAdapter(val items: ArrayList<CommandListItemData>, val context: Context)
    : RecyclerView.Adapter<ViewHolderCommandList>() {

    override fun onBindViewHolder(holder: ViewHolderCommandList, position: Int) {
        holder?.bindItems(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCommandList {
        return ViewHolderCommandList(LayoutInflater.from(context).inflate(R.layout.item_list_command, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolderCommandList(view: View) : RecyclerView.ViewHolder(view) {

    fun bindItems(item: CommandListItemData) {
        itemView.tv_name.text = item.name
    }
}