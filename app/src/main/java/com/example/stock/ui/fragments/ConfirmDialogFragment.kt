package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.stock.R

class ConfirmDialogFragment : DialogFragment() {
    
    private var onConfirm: ((Boolean) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm_dialog, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val titleText = view.findViewById<TextView>(R.id.title_text)
        val messageText = view.findViewById<TextView>(R.id.message_text)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        
        // 设置对话框内容
        titleText.text = arguments?.getString(ARG_TITLE) ?: "确认"
        messageText.text = arguments?.getString(ARG_MESSAGE) ?: "您确定要继续吗？"
        
        // 设置按钮监听
        cancelButton.setOnClickListener {
            onConfirm?.invoke(false)
            dismiss()
        }
        
        confirmButton.setOnClickListener {
            onConfirm?.invoke(true)
            dismiss()
        }
    }
    
    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        
        fun newInstance(
            title: String,
            message: String,
            onConfirm: (Boolean) -> Unit
        ) = ConfirmDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
            this.onConfirm = onConfirm
        }
    }
} 