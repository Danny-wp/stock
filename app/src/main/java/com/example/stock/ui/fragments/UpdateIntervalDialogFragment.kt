package com.example.stock.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.stock.R

class UpdateIntervalDialogFragment : DialogFragment() {
    
    private var onIntervalSelected: ((Int) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_interval_dialog, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val radioGroup = view.findViewById<RadioGroup>(R.id.interval_radio_group)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        
        // 设置当前选中的间隔
        val currentInterval = arguments?.getInt(ARG_INTERVAL) ?: 15
        
        when (currentInterval) {
            15 -> view.findViewById<RadioButton>(R.id.radio_15min).isChecked = true
            30 -> view.findViewById<RadioButton>(R.id.radio_30min).isChecked = true
            60 -> view.findViewById<RadioButton>(R.id.radio_60min).isChecked = true
            120 -> view.findViewById<RadioButton>(R.id.radio_120min).isChecked = true
        }
        
        // 设置按钮监听
        cancelButton.setOnClickListener { dismiss() }
        
        saveButton.setOnClickListener {
            val selectedInterval = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_15min -> 15
                R.id.radio_30min -> 30
                R.id.radio_60min -> 60
                R.id.radio_120min -> 120
                else -> 15
            }
            
            onIntervalSelected?.invoke(selectedInterval)
            dismiss()
        }
    }
    
    companion object {
        private const val ARG_INTERVAL = "interval"
        
        fun newInstance(
            currentInterval: Int,
            onIntervalSelected: (Int) -> Unit
        ) = UpdateIntervalDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_INTERVAL, currentInterval)
            }
            this.onIntervalSelected = onIntervalSelected
        }
    }
} 