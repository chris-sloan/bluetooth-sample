package com.chrissloan.bluetoothconnection.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chrissloan.bluetoothconnection.R
import com.chrissloan.bluetoothconnection.databinding.FragmentHomeBinding
import com.chrissloan.bluetoothconnection.dependencies.android.permissions.SystemPermissionsRequestHandler
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    private val systemPermissionsRequestHandler = SystemPermissionsRequestHandler.from(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener {
            systemPermissionsRequestHandler
                .request(homeViewModel.requiredPermissions)
                .rationale(getString(R.string.bluetooth_request_rationale))
                .checkDetailedPermission { wasGranted ->
                    if (wasGranted.all { it.value }) {
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
            Timber.d("viewState -> $it")
            when {
                it.isLoading -> {}
            }
        })
    }
}
