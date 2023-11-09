package com.example.karsoftrivojyulduz.presentation.ui.histories

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.karsoftrivojyulduz.R
import com.example.karsoftrivojyulduz.databinding.FragmentHistoriesBinding
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.presentation.ui.histories.adapter.HistoriesAdapter
import com.example.karsoftrivojyulduz.presentation.ui.viewmodel.OrdersAndHistoriesViewModel
import com.example.karsoftrivojyulduz.util.constant.Constants
import com.example.karsoftrivojyulduz.util.extension.toastMessage
import com.example.karsoftrivojyulduz.util.local.LocalStorage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoriesFragment : Fragment(R.layout.fragment_histories) {

    private lateinit var historiesAdapter: HistoriesAdapter

    private var _binding: FragmentHistoriesBinding? = null

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
        initHistoriesAdapter()
        setUpRecyclerViewLayoutManager()
        setUpRecyclerViewAdapter()
    }

    private fun turnOnSwipeRefreshEffect() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun turnOffSwipeRefreshEffect() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun setUpRecyclerViewLayoutManager() {
        val layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerViewHistory.layoutManager = layoutManager
    }

    private fun initObservables() {
        with(ordersViewModel) {
            successFlow.onEach {
                turnOffSwipeRefreshEffect()
                initListHistoriesAdapter(it.data)
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

    private fun getAllData() {
        turnOnSwipeRefreshEffect()
        lifecycleScope.launch {
            ordersViewModel.getAllOrders(Constants.ORDER_STATUS_DONE)
        }
    }

    private fun bindView(view: View) {
        _binding = FragmentHistoriesBinding.bind(view)
    }

    private fun unBindView() {
        _binding = null
    }

    private fun initHistoriesAdapter() {
        historiesAdapter = HistoriesAdapter(requireContext())
    }

    private fun setUpRecyclerViewAdapter() {
        binding.recyclerViewHistory.adapter = historiesAdapter
    }

    private fun initListHistoriesAdapter(listOfHistory: List<OrderAndHistoryResponseData.Data>) {
        historiesAdapter.submitList(listOfHistory)
    }

    private fun initListeners() {
        with(binding) {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            swipeRefreshLayout.setOnRefreshListener {
                getAllData()
            }
            historiesAdapter.setOnItemClickListener { data ->
                val direction =
                    HistoriesFragmentDirections.actionHistoryFragmentToOrderFragment(
                        data.id,
                        true,
                        (data.contact.title ?: "Не указан").toString(),
                        (data.contact.address ?: "Не указан").toString(),
                        data.contact.name,
                        data.contact.phone,
                        data.statusId
                    )
                navigateTo(direction)
            }
        }
    }

    private fun navigateTo(direction: NavDirections) {
        findNavController().navigate(direction)
    }

    private fun checkAndChangeVisibilityWhenOrderListIsEmpty(listOfHistory: List<OrderAndHistoryResponseData.Data>) {
        with(binding) {
            if (listOfHistory.isEmpty()) {
                noDataContainer.visibility = View.VISIBLE
                recyclerViewHistory.visibility = View.INVISIBLE
            } else {
                noDataContainer.visibility = View.INVISIBLE
                recyclerViewHistory.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getAllData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBindView()
        LocalStorage().fromOrdersFragment = false
    }
}