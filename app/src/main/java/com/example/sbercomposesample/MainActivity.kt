package com.example.sbercomposesample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Условие задания:
 *
 * Есть экран с загрузкой данных.
 * При встряхивании телефона необходимо выполнить загрузку заново.
 * Есть поле ввода для значения поиска. При нажатии на кнопку запрос данных с поиском.
 * Если пролистали первый элемент списка, то показывается сообщение "Вернись к поиску"
 */
class MainActivity : ComponentActivity() {

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = MainScreenViewModel()
            MainScreen(
                state = viewModel.state.value,
                loadData = { viewModel.loadData(it) }
            )
        }
    }
}