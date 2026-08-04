package com.example.counterforuno

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _scannedCards = MutableLiveData<MutableList<YoloTfliteDetector.Detection>>(mutableListOf())
    val scannedCards: LiveData<MutableList<YoloTfliteDetector.Detection>> = _scannedCards

    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    private val _isWaitingForChoice = MutableLiveData(false)
    val isWaitingForChoice: LiveData<Boolean> = _isWaitingForChoice

    private val _lastCardText = MutableLiveData(application.getString(R.string.scan_camera))
    val lastCardText: LiveData<String> = _lastCardText

    fun addCard(detection: YoloTfliteDetector.Detection) {
        val currentList = _scannedCards.value ?: mutableListOf()
        currentList.add(detection)
        _scannedCards.value = currentList

        _lastCardText.value = "📷 ${detection.clsName}"
    }

    fun removeLastCard(): YoloTfliteDetector.Detection? {
        val currentList = _scannedCards.value ?: return null
        if (currentList.isEmpty()) return null

        val removed = currentList.removeAt(currentList.size - 1)
        _scannedCards.value = currentList

        if (currentList.isNotEmpty()) {
            _lastCardText.value = "${getApplication<Application>().getString(R.string.last)} ${currentList.last().clsName}"
        } else {
            _lastCardText.value = getApplication<Application>().getString(R.string.scan_camera)
        }

        return removed
    }

    fun clearAll() {
        _scannedCards.value = mutableListOf()
        _lastCardText.value = getApplication<Application>().getString(R.string.scan_camera)
    }

    fun getTotalScore(): Int {
        return _scannedCards.value?.sumOf { it.score } ?: 0
    }

    fun getCardListString(): String {
        val cards = _scannedCards.value ?: return ""
        if (cards.isEmpty()) return getApplication<Application>().getString(R.string.no_cards_yet)

        val sb = StringBuilder()
        cards.forEachIndexed { index, detection ->
            sb.append("${index + 1}. ${detection.clsName}\n")
            sb.append("   💰 ${detection.score}" +  getApplication<Application>().getString(R.string.score) + "\n")
            if (index < cards.size - 1) sb.append("   ───────────\n")
        }
        return sb.toString()
    }

    fun setScanning(isScanning: Boolean) {
        _isScanning.value = isScanning
    }

    fun setWaitingForChoice(isWaiting: Boolean) {
        _isWaitingForChoice.value = isWaiting
    }

    fun setLastCardText(text: String) {
        _lastCardText.value = text
    }

    fun hasCards(): Boolean {
        return !(_scannedCards.value?.isEmpty() ?: true)
    }

    fun getCardCount(): Int {
        return _scannedCards.value?.size ?: 0
    }


}