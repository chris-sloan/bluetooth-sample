package com.chrissloan.bluetoothconnection.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chrissloan.bluetoothconnection.R
import com.chrissloan.bluetoothconnection.databinding.FragmentHomeBinding
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.SystemPermissionsRequestHandler
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    private val systemPermissionsRequestHandler = SystemPermissionsRequestHandler.from(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener {
            systemPermissionsRequestHandler
                .request(homeViewModel.requiredPermissions)
                .rationale(getString(R.string.bluetooth_request_rationale))
                .checkDetailedPermission { wasGranted ->
                    if (wasGranted.all { it.value } ) {
                        homeViewModel.findBluetoothDevices()
                    } else {
                        homeViewModel.permissionRequestDenied()
                    }
                }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.viewState.observe(viewLifecycleOwner, {
            when {
                it.isLoading -> {}
            }
        })
    }
}
