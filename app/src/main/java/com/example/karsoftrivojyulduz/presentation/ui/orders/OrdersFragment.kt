package com.example.karsoftrivojyulduz.presentation.ui.orders

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentOrdersBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.presentation.ui.dialog.logout.LogOutDialog
import com.example.karsoftrivojyulduz.presentation.ui.orders.adapter.OrdersAdapter
import com.example.karsoftrivojyulduz.presentation.ui.viewmodel.OrdersAndHistoriesViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import com.example.karsoftrivojyulduz.util.local.LocalStorage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var logOutDialog: LogOutDialog

    private var _binding: FragmentOrdersBinding? = null

    private val binding get() = _binding!!
    private val ordersViewModel by viewModel<OrdersAndHistoriesViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        initValues()
        initObservables()
        initListeners()
    }

    private fun initValues() {
        changeSystemBarsAndIconsColor()
        initOrdersAdapter()
        setUpRecyclerViewLayoutManager()
        setUpRecyclerViewAdapter()
        initLogOutDialog()
        setUpOnBackPressedCallback()
    }

    private fun setUpOnBackPressedCallback() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, onBackPressedCallback
        )
    }

    private fun initLogOutDialog() {
        logOutDialog = LogOutDialog()
    }

    private fun turnOnSwipeRefreshEffect() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun initObservables() {
        with(ordersViewModel) {
            successFlow.onEach {
                turnOffSwipeRefreshEffect()
                initAdapterList(it.data)
                checkAndChangeVisibilityWhenOrderListIsEmpty(it.data)
            }.launchIn(lifecycleScope)
            messageFlow.onEach {
                turnOffSwipeRefreshEffect()
            }.launchIn(lifecycleScope)
            errorFlow.onEach {
                turnOffSwipeRefreshEffect()
            }.launchIn(lifecycleScope)
        }
    }

    private fun turnOffSwipeRefreshEffect() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun setUpRecyclerViewLayoutManager() {
        val layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )

        binding.recyclerViewOrders.layoutManager = layoutManager
    }

    private fun initAdapterList(it: List<OrderAndHistoryResponseData.Data>) {
        val newOrdersList = it.filter { it.statusId == Constants.METER_ATTACHED }.sortedByDescending { it.id }
        val redoOrderList = it.filter { it.statusId == Constants.REDO_THE_WORK }.sortedByDescending { it.id }
        val resultList = mutableListOf<OrderAndHistoryResponseData.Data>().apply {
            addAll(newOrdersList)
            addAll(redoOrderList)
        }
        ordersAdapter.submitList(resultList)
    }

    private fun changeSystemBarsAndIconsColor() {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            with(ViewCompat.getWindowInsetsController(window.decorView)!!) {
                isAppearanceLightNavigationBars = true
                isAppearanceLightStatusBars = false
            }
        }
    }

    private fun bindView(view: View) {
        _binding = FragmentOrdersBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun initOrdersAdapter() {
        ordersAdapter = OrdersAdapter()
    }

    private fun setUpRecyclerViewAdapter() {
        binding.recyclerViewOrders.adapter = ordersAdapter
    }

    private fun showLogOutDialog() {
        logOutDialog.show(childFragmentManager, null)
    }

    private fun dismissLogOutDialog() {
        logOutDialog.dismiss()
    }

    private fun initListeners() {
        with(binding) {
            ivHistory.setOnClickListener {
                navigateTo(R.id.action_mainFragment_to_historyFragment)
            }
            ivLogOut.setOnClickListener {
                showLogOutDialog()
            }
            ordersAdapter.setOnItemClickListener { data ->
                val direction = OrdersFragmentDirections.actionMainFragmentToOrderFragment(
                    data.id,
                    false,
                    (data.contact.title ?: "Не указен").toString(),
                    (data.contact.address ?: "Не указен").toString(),
                    data.contact.name,
                    data.contact.phone,
                    data.statusId
                )
                navigateTo(direction)
            }
            swipeRefreshLayout.setOnRefreshListener {
                getAllData()
            }
            logOutDialog.onYesButtonClickListener {
                LocalStorage().fromOrdersFragment = true
                if (LocalStorage().isLogin)
                    LocalStorage().isLogin = false

                if (!LocalStorage().isLogin)
                    navigateTo(R.id.signInFragment)
            }
            logOutDialog.onNoButtonClickListener {
                dismissLogOutDialog()
            }
        }
    }

    private fun checkAndChangeVisibilityWhenOrderListIsEmpty(listOfOrder: List<OrderAndHistoryResponseData.Data>) {
        with(binding) {
            if (listOfOrder.isEmpty()) {
                noDataContainer.visibility = View.VISIBLE
                recyclerViewOrders.visibility = View.INVISIBLE
            } else {
                noDataContainer.visibility = View.INVISIBLE
                recyclerViewOrders.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateTo(direction: Int) {
        findNavController().navigate(direction)
    }

    private fun navigateTo(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun getAllData() {
        turnOnSwipeRefreshEffect()
        lifecycleScope.launch {
            ordersViewModel.getAllOrders(Constants.ORDER_STATUS_PROCESS)
        }
    }

    override fun onResume() {
        super.onResume()
        getAllData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}