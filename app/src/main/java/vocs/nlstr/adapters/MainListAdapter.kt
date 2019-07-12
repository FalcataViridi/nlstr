package vocs.nlstr.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.view.menu.MenuView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_list_main.view.*
import vocs.nlstr.R
import vocs.nlstr.models.MainListItemData
import vocs.nlstr.utils.MainListItemAttributes.DESCRIPCION
import vocs.nlstr.utils.MainListItemAttributes.TITULO

class MainListAdapter(val items: ArrayList<MainListItemData>, val context: Context)
    : RecyclerView.Adapter<ViewHolderMainList>() {


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolderMainList, position: Int) {
        holder?.bindItems(items[position])
    }

    var elementChanging = ""
    var newText = ""
    var positionSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMainList {
        return ViewHolderMainList(LayoutInflater.from(context).inflate(R.layout.item_list_main, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolderMainList, position: Int, payloads: MutableList<Any>) {

        holder.deactivating()

        if (payloads.isEmpty()) {
            holder.itemView.requestFocus()

            if (positionSelected == position) holder.activateRow()
            onBindViewHolder(holder, position)

        } else {
            when (payloads[0]) {
                TITULO.name -> {
                    holder.itemView.tv_name.isActivated = true
                    items[position].name = newText
                }

                DESCRIPCION.name -> {
                    holder.itemView.tv_desc.isActivated = true
                    items[position].description = newText
                }

                /*STATUS.name -> {
                    holder.itemView. .isActivated = true
                    items[position].description = newText
                }

                TIPO.name -> {
                    holder.itemView.tv_desc.isActivated = true
                    items[position].description = newText
                }*/
            }

            //STATUS.name -> holder?.setTitle(title)
            //TIPO.name -> holder?.setTitle(title)
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    fun insert(position: Int, item: MainListItemData, element: String) {
        items.add(position, item)
        elementChanging = element
        positionSelected = position

        notifyItemInserted(position)
    }

    fun deactivateView(position: Int) {
        notifyItemChanged(position)
    }

    fun updateingInfo(position: Int, text: String) {
        newText = text
        notifyItemChanged(position, elementChanging)
    }

    fun activateElement(position: Int, element: String) {
        elementChanging = element
        notifyItemChanged(position, elementChanging)
    }

    fun removeByItem(item: MainListItemData) {
        val position = items.indexOf(item)
        items.remove(item)
        notifyItemRemoved(position)
    }

    fun removeByPosition(position: Int) {
        notifyItemRemoved(position)
    }

    fun selectItem(position: Int) {
        positionSelected = position
        notifyItemChanged(position)
    }
}

class ViewHolderMainList(view: View) : RecyclerView.ViewHolder(view) {

    fun bindItems(item: MainListItemData) {
        itemView.tv_name.text = item.name
        itemView.tv_desc.text = item.description
    }

    fun deactivating() {
        itemView.isActivated = false
        itemView.tv_name.isActivated = false
        itemView.tv_desc.isActivated = false
        itemView.userImg.isActivated = false
        itemView.iv_status.isActivated = false
    }

    fun activateRow() {
        itemView.singleRow.isSelected = true
        itemView.tv_name.isSelected = false
        itemView.tv_desc.isSelected = false
        itemView.userImg.isSelected = false
        itemView.iv_status.isSelected = false
    }

}