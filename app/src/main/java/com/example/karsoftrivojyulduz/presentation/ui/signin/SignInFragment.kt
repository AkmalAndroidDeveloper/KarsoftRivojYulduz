package com.example.karsoftrivojyulduz.presentation.ui.signin

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentSignInBinding
import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.loading.LoadingDialog
import com.example.karsoftrivojyulduz.presentation.ui.signin.viewmodel.SignInViewModel
import com.example.karsoftrivojyulduz.util.Constants
import com.example.karsoftrivojyulduz.util.LocalStorage
import com.example.karsoftrivojyulduz.util.MaskWatcher
import com.example.karsoftrivojyulduz.util.toastMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private lateinit var loadingDialog: LoadingDialog

    private var _binding: FragmentSignInBinding? = null

    private val binding get() = _binding!!
    private val signInViewModel by viewModel<SignInViewModel>()

    companion object {
        private const val TAG = "SignInFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        changeSystemBarsAndIconsColor()
        setUpMaskForInputPhoneNumber()
        initLoadingDialog()
        initListeners()
        initObservables()

        if (LocalStorage().isLogin)
            navigateTo(R.id.action_signInFragment_to_mainFragment)
    }

    private fun initLoadingDialog() {
        loadingDialog = LoadingDialog()
    }

    private fun initObservables() {
        with(signInViewModel) {
            successFlow.onEach {
                saveUserDataToStorage(it)
                loadingDialog.dismiss()
                navigateTo(R.id.action_signInFragment_to_mainFragment)
            }.launchIn(lifecycleScope)
            messageFlow.onEach {
                toastMessage("Номер телефона или пароль введены неверно")
                Log.d(TAG, "Message: $it")
            }.launchIn(lifecycleScope)
            errorFlow.onEach {
                toastMessage("Номер телефона или пароль введены неверно")
                Log.d(TAG, "Error: $it")
            }.launchIn(lifecycleScope)
        }
    }

    private fun saveUserDataToStorage(it: SignInResponseData) {
        LocalStorage().token = it.data.token
    }

    private fun initListeners() {
        with(binding) {
            btnSignIn.setOnClickListener {
                loadingDialog.show(childFragmentManager, null)
                signIn()
            }
        }
    }

    private fun signIn() {
        with(binding) {
            val phoneNumber = "${Constants.NUMBER_PREFIX}${
                etPhone.text.toString().filter { it.isDigit() }.trim()
            }"
            val password = etPassword.text.toString().trim()
            val body = SignInRequestData(phoneNumber, password)

            lifecycleScope.launch {
                signInViewModel.signIn(body)
            }
        }
    }

    private fun bindView(view: View) {
        _binding = FragmentSignInBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun changeSystemBarsAndIconsColor() {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        with(ViewCompat.getWindowInsetsController(window.decorView)!!) {
            isAppearanceLightNavigationBars = true
            isAppearanceLightStatusBars = true
        }
    }

    private fun navigateTo(direction: Int) {
        findNavController().navigate(direction)
    }

    private fun navigateTo(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun setUpMaskForInputPhoneNumber() {
        binding.etPhone.addTextChangedListener(MaskWatcher("## ###-##-##"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}