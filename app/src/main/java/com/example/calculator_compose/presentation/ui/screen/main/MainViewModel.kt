package com.example.calculator_compose.presentation.ui.screen.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.calculator_compose.domain.interactor.MainInteractor
import com.example.calculator_compose.domain.model.PresentationValues
import com.example.calculator_compose.navigation.NavigationTree
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class MainViewModel @Inject constructor(
    private val interactor: MainInteractor, private val dispatcher: CoroutineContext
) : ViewModel() {

    var example: MutableLiveData<String> = MutableLiveData(interactor.getCalculation().calculation)
    var history: Flow<String?> = loadHistory()
    private var action: String = interactor.getCalculation().action

    fun numberPress(text: String) {
        example.value = interactor.number(
            text = text, action = action, example = example.value.toString()
        )
    }

    fun zeroPress() {
        example.value = interactor.zero(example = example.value.toString())
    }

    fun comaPress() {
        example.value = interactor.coma(example = example.value.toString(), action = action)
    }

    fun actionPress(text: String) {
        val values = interactor.action(
            text = text, example = example.value.toString(), action = action
        )

        example.value = values.calculation
        action = values.action
    }

    fun equalPress() {
        val allValues = interactor.equal(
            example = example.value.toString(), operation = action, history = ""
        )

        example.value = allValues.calculation
        action = allValues.action

        viewModelScope.launch(dispatcher) {
            saveHistory(history.first() + allValues.history)
        }
    }

    fun exampleBack() {
        val values = interactor.back(example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun exampleClear() {
        example.value = "null"
        action = ""
    }

    fun navigationToAdditional(navController: NavController) {
        navController.navigate(NavigationTree.Additional.name)
        interactor.setCalculation(
            PresentationValues(
                calculation = example.value.toString(), action = action
            )
        )
    }

    private fun saveHistory(history: String) = viewModelScope.launch(dispatcher) {
        interactor.storeHistory().save(history)
    }

    private fun loadHistory() = interactor.storeHistory().get()
}