package com.example.karsoftrivojyulduz.presentation.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.presentation.ui.orders.adapter.OrdersAdapter
import com.example.karsoftrivojyulduz.databinding.FragmentOrdersBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.Data
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.presentation.ui.viewmodel.OrdersAndHistoriesViewModel
import com.example.karsoftrivojyulduz.util.Constants
import com.example.karsoftrivojyulduz.util.toastMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private lateinit var ordersAdapter: OrdersAdapter

    private var _binding: FragmentOrdersBinding? = null

    private val binding get() = _binding!!
    private val ordersViewModel by viewModel<OrdersAndHistoriesViewModel>()

    companion object {
        private const val TAG = "OrdersFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)

        binding.swipeRefreshLayout.isRefreshing = true

        changeSystemBarsAndIconsColor()
        initOrdersAdapter()
        initObservables()
        initListeners()
        getAllData()
    }

    private fun initObservables() {
        with(ordersViewModel) {
            successFlow.onEach {
                binding.swipeRefreshLayout.isRefreshing = false
                initAdapterList(it.data)
                checkAndChangeVisibilityWhenOrderListIsEmpty(it.data)
                setUpRecyclerViewLayoutManager()
                setUpRecyclerViewAdapter()
            }.launchIn(lifecycleScope)
            messageFlow.onEach {
                binding.swipeRefreshLayout.isRefreshing = false
                Log.d(TAG, "MessageFlow: $it")
            }.launchIn(lifecycleScope)
            errorFlow.onEach {
                binding.swipeRefreshLayout.isRefreshing = false
                Log.d(TAG, "ErrorFlow: $it")
            }.launchIn(lifecycleScope)
        }
    }

    private fun setUpRecyclerViewLayoutManager() {
        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.recyclerViewOrders.layoutManager = layoutManager
    }

    private fun initAdapterList(it: List<OrderAndHistoryResponseData.Data>) {
        ordersAdapter.submitList(it)
    }

    private fun changeSystemBarsAndIconsColor() {
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.blue)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        with(ViewCompat.getWindowInsetsController(window.decorView)!!) {
            isAppearanceLightNavigationBars = true
            isAppearanceLightStatusBars = false
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

    private fun initListeners() {
        with(binding) {
            ivHistory.setOnClickListener {
                navigateTo(R.id.action_mainFragment_to_historyFragment)
            }
            ordersAdapter.setOnItemClickListener { order, orderId ->
                val direction =
                    OrdersFragmentDirections.actionMainFragmentToOrderFragment(orderId, false)
                navigateTo(direction)
            }
            swipeRefreshLayout.setOnRefreshListener {
                getAllData()
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
        lifecycleScope.launch {
            ordersViewModel.getAllOrders(Constants.ORDER_STATUS_PROCESS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
    }
}