package com.chrissloan.bluetoothconnection.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chrissloan.bluetoothconnection.R
import com.chrissloan.bluetoothconnection.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.viewState.observe(viewLifecycleOwner, {
            Log.d("HomeFragment", "view State incoming : $it")
        })
        return root
    }
}
