package com.airprint.smart.printer.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airprint.smart.printer.R
import com.airprint.smart.printer.databinding.FragmentHelpFirstBinding
import com.airprint.smart.printer.databinding.FragmentHelpSecondBinding

class HelpSecondFragment : BaseFragment<FragmentHelpSecondBinding>() {
    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHelpSecondBinding {
        return FragmentHelpSecondBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.post {
            inItView()
        }
    }

    private fun inItView() {

    }
}