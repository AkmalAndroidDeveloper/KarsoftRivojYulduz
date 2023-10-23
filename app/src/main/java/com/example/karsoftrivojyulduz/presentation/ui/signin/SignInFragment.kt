package com.example.karsoftrivojyulduz.presentation.ui.signin

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentSignInBinding
import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.loading.LoadingDialog
import com.example.karsoftrivojyulduz.presentation.ui.signin.viewmodel.SignInViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import com.example.karsoftrivojyulduz.util.local.LocalStorage
import com.example.karsoftrivojyulduz.util.validator.MaskWatcher
import com.example.karsoftrivojyulduz.util.validator.PasswordValidator
import com.example.karsoftrivojyulduz.util.validator.PhoneNumberValidator
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
        setUpOnBackPressedCallback()

        if (!LocalStorage().fromOrdersFragment) if (LocalStorage().isLogin) {
            navigateTo(R.id.action_signInFragment_to_mainFragment)
        }
    }

    private fun setUpOnBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }

    private fun initLoadingDialog() {
        loadingDialog = LoadingDialog()
    }

    private fun initObservables() {
        with(signInViewModel) {
            successFlow.onEach {
                saveUserDataToStorage(it)
                dismissLoadingDialog()
                navigateTo(R.id.action_signInFragment_to_mainFragment)
            }.launchIn(lifecycleScope)
            messageFlow.onEach {
                toastMessage(it)
                Log.d(TAG, "Message: $it")
                dismissLoadingDialog()
            }.launchIn(lifecycleScope)
            errorFlow.onEach {
                Log.d(TAG, "Error: $it")
            }.launchIn(lifecycleScope)
        }
    }

    private fun finish() {
        requireActivity().finish()
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    private fun saveUserDataToStorage(it: SignInResponseData) {
        LocalStorage().token = it.data.token
        LocalStorage().isLogin = true
    }

    private fun initListeners() {
        with(binding) {
            btnSignIn.setOnClickListener {
                val phoneNumber = "${Constants.NUMBER_PREFIX}${
                    etPhone.text.toString().filter { it.isDigit() }.trim()
                }"
                val password = etPassword.text.toString().trim()

                if (userInputsSuccessfullyValidated(phoneNumber, password)) {
                    showLoadingDialog()
                    signIn(phoneNumber, password)
                    clearUserInputsData()
                    disableUserInputsErrors()

                    LocalStorage().fromOrdersFragment = false
                    tilPhone.isFocusable = false
                    tilPassword.isFocusable = false
                } else showUserInputErrors(phoneNumber, password)
            }
        }
    }

    private fun showUserInputErrors(phoneNumber: String, password: String) {
        with(binding) {
            if (PhoneNumberValidator(phoneNumber).hasLengthEmpty()) tilPhone.error =
                "Номер телефона не должен быть пустым"
            else if (PhoneNumberValidator(phoneNumber).hasEnteredLessValueThanRequired()) tilPhone.error =
                "Номер телефона должен состоять из 9 символов"
            else tilPhone.isErrorEnabled = false
            if (PasswordValidator(password).hasLengthEmpty()) tilPassword.error =
                "Пароль не должен быть пустым"
            else if (PasswordValidator(password).hasCyrillicLetters()) tilPassword.error =
                "Пароль не должен содержать символов кириллицы"
            else if (PasswordValidator(password).hasEnteredMoreValueThanRequired()) tilPassword.error =
                "Пароль введен больше, чем необходимо"
            else tilPassword.isErrorEnabled = false
        }
    }

    private fun userInputsSuccessfullyValidated(phoneNumber: String, password: String): Boolean {
        return !PhoneNumberValidator(phoneNumber).hasLengthEmpty() && !PhoneNumberValidator(
            phoneNumber
        ).hasEnteredLessValueThanRequired() && !PasswordValidator(password).hasLengthEmpty() && !PasswordValidator(
            password
        ).hasCyrillicLetters() && !PasswordValidator(password).hasEnteredMoreValueThanRequired()
    }

    private fun showLoadingDialog() {
        loadingDialog.show(childFragmentManager, null)
    }

    private fun signIn(phoneNumber: String, password: String) {
        val body = SignInRequestData(phoneNumber, password)
        lifecycleScope.launch {
            signInViewModel.signIn(body)
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
        val decorView = window.decorView
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            with(ViewCompat.getWindowInsetsController(decorView)!!) {
                isAppearanceLightNavigationBars = true
                isAppearanceLightStatusBars = true
            }
        } else {

        }
    }

    private fun navigateTo(direction: Int) {
        findNavController().navigate(direction)
    }

    private fun setUpMaskForInputPhoneNumber() {
        binding.etPhone.addTextChangedListener(MaskWatcher("## ###-##-##"))
    }

    private fun clearUserInputsData() {
        with(binding) {
            etPhone.text?.clear()
            etPassword.text?.clear()
        }
    }

    private fun disableUserInputsErrors() {
        with(binding) {
            tilPassword.isErrorEnabled = false
            tilPhone.isErrorEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}