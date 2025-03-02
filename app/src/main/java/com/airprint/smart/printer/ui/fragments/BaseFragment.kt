package com.airprint.smart.printer.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding


abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    protected lateinit var binding: VB
    private lateinit var animatorSetEnableBtn: AnimatorSet
    private lateinit var scaleX: ObjectAnimator
    private lateinit var scaleY: ObjectAnimator

    val TAG="XYZ_FRAGMENT"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = setupViewBinding(inflater, container)

        return binding.root
    }



    abstract fun setupViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    fun setUpAnimationOnVies(view: View){
        animatorSetEnableBtn= AnimatorSet()
        scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.1f, 0.9f)
        scaleY = ObjectAnimator.ofFloat(view,"scaleY", 0.9f, 1.1f, 0.9f)
        scaleX.repeatCount = ObjectAnimator.INFINITE
        scaleX.duration = 2000
        scaleY.repeatCount = ObjectAnimator.INFINITE
        scaleY.duration = 2000

        animatorSetEnableBtn.playTogether(scaleX, scaleY)
        animatorSetEnableBtn.start()
    }
}