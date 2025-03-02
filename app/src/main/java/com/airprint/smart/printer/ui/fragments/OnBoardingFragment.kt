package com.airprint.smart.printer.ui.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airprint.smart.printer.R
import com.airprint.smart.printer.databinding.FragmentOnBoardingBinding
import com.airprint.smart.printer.utils.Constent
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OnBoardingFragment : Fragment() {
    val binding: FragmentOnBoardingBinding by lazy {
        FragmentOnBoardingBinding.inflate(layoutInflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val pos: Int = requireArguments().getInt(Constent.ARG_POSITION_ADP)
        when (pos) {
            0 -> {
                Glide.with(this).load(R.drawable.first_iv).into(binding.mainIv)
                binding.mainTv.text=getString(R.string.print_anywhere_any_thing)
                 binding.question1.text=getString(R.string._01)
                 binding.ans1.text=getString(R.string.ans1)
                 binding.question2.text=getString(R.string._02)
                 binding.ans2.text=getString(R.string.ans2)


            }

            1 -> {
                Glide.with(this).load(R.drawable.second_icon).into(binding.mainIv)
                binding.mainTv.text=getString(R.string.how_to_use)

                binding.question1.text=getString(R.string.step_01)
                binding.question2.text=getString(R.string.step_02)
                binding.question3.text=getString(R.string.step_03)

                binding.ans1.text=getString(R.string.step_01_ans)
                binding.ans2.text=getString(R.string.step_02_ans)
                binding.ans3.text=getString(R.string.step_03_ans)
            }


        }
    }




}