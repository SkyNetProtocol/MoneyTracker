package com.example.loginapp.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.loginapp.databinding.FragmentSplashBinding
import com.example.loginapp.presentation.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initial state for animation
        binding.logoImageView.alpha = 0f
        binding.logoImageView.scaleX = 0.5f
        binding.logoImageView.scaleY = 0.5f

        // Start animation
        binding.logoImageView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .start()

        // Navigate to Login after delay
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) // Wait for 2 seconds
            if (isAdded) {
                parentFragmentManager.beginTransaction()
                    .replace(com.example.loginapp.R.id.fragment_container_view, LoginFragment())
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
