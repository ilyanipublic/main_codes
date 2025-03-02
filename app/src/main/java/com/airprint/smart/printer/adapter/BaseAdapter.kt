package com.airprint.smart.printer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


abstract class BaseAdapter<Model, VB : ViewBinding> :
    RecyclerView.Adapter<BaseAdapter<Model, VB>.GenericViewHolder>() {

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB

    abstract fun bind(binding: VB, item: Model,position: Int)

    private var list: ArrayList<Model> = ArrayList()

    private var onItemClickListener: ((binding:VB, Model, Int) -> Unit)? = null

    fun setData(newData: ArrayList<Model>) {
        list.clear()
        list = newData
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (VB,Model, Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val binding = createBinding(LayoutInflater.from(parent.context), parent)
        return GenericViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        bind(holder.binding, list[position],position)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(holder.binding,list[position], position)
            notifyDataSetChanged()
        }


    }

    override fun getItemCount(): Int = list.size

    inner class GenericViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)
}