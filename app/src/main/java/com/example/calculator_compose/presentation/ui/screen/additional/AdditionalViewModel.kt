package com.example.calculator_compose.presentation.ui.screen.additional

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.calculator_compose.app.Strings
import com.example.calculator_compose.domain.interactor.AdditionalInteractor
import com.example.calculator_compose.domain.model.PresentationValues
import com.example.calculator_compose.navigation.NavigationTree
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class AdditionalViewModel @Inject constructor(
    private val interactor: AdditionalInteractor, private val dispatcher: CoroutineContext
) : ViewModel() {

    var example = MutableLiveData(interactor.getCalculation().calculation)
    val degrees = MutableLiveData(interactor.converting())
    val history: Flow<String?> = loadHistory()

    val degreesEnabled = MutableLiveData(true)

    val sinText = MutableLiveData(Strings.ACTION_SIN)
    val cosText = MutableLiveData(Strings.ACTION_COS)
    val tanText = MutableLiveData(Strings.ACTION_TAN)

    private var action: String = interactor.getCalculation().action

    fun numberPress(text: String) {
        example.value = interactor.number(
            text = text, action = action, example = example.value.toString()
        )
    }

    fun letterNumPress(text: String) {
        example.value = interactor.letterNum(
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
        val values =
            interactor.action(text = text, example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun squareRootPress() {
        val values = interactor.sqrt(example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun factorialPress() {
        val values = interactor.factorial(example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun trigonometricPress(text: String) {
        val values = interactor.trigonometric(
            text = text, example = example.value.toString(), action = action
        )

        example.value = values.calculation
        action = values.action
    }

    fun rightBracket() {
        val values = interactor.rightBracket(example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun leftBracket() {
        val values = interactor.leftBracket(example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun equalPress() {
        var newHistory = ""

        val allValues = interactor.equal(
            example = example.value.toString(),
            operation = action,
            history = newHistory,
            isRadians = degrees.value.toString()
        )

        example.value = allValues.calculation
        action = allValues.action
        newHistory += allValues.history

        viewModelScope.launch(dispatcher) {
            saveHistory(history.first() + newHistory)
        }
    }

    fun exampleBack() {
        val values = interactor.back(example = example.value.toString(), action = action)

        example.value = values.calculation
        action = values.action
    }

    fun exampleClear() {
        example.value = Strings.START_EXAMPLE
        action = ""
    }

    fun converting() {
        degrees.value = interactor.converting()
    }

    fun twoND() {
        degreesEnabled.value = !degreesEnabled.value!!

        if (degreesEnabled.value == true) {
            sinText.value = Strings.ACTION_SIN
            cosText.value = Strings.ACTION_COS
            tanText.value = Strings.ACTION_TAN
        } else if (degreesEnabled.value == false) {
            sinText.value = Strings.ACTION_ARCSIN
            cosText.value = Strings.ACTION_ARCCOS
            tanText.value = Strings.ACTION_ARCTAN
        }

    }

    fun enabledTwoND() = degrees.value != Strings.RADIANS

    fun navigationToAdditional(navController: NavController) {
        navController.navigate(NavigationTree.Main.name)
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