package com.example.ui.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OcrViewModel : ViewModel() {

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _rotationAngle = MutableStateFlow(0)
    val rotationAngle: StateFlow<Int> = _rotationAngle.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _resultText = MutableStateFlow("")
    val resultText: StateFlow<String> = _resultText.asStateFlow()

    fun setImageBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
        _resultText.value = ""
    }

    fun cycleRotation() {
        val angles = OcrEngine.SUPPORTED_ROTATIONS
        val currentIndex = angles.indexOf(_rotationAngle.value)
        val nextIndex = (if (currentIndex == -1) 0 else currentIndex + 1) % angles.size
        _rotationAngle.value = angles[nextIndex]
    }

    fun setRotation(angle: Int) {
        if (OcrEngine.SUPPORTED_ROTATIONS.contains(angle)) {
            _rotationAngle.value = angle
        }
    }

    fun recognize() {
        val bitmap = _selectedBitmap.value ?: return
        viewModelScope.launch {
            _isRecognizing.value = true
            val ocr = OcrEngine.recognizeText(bitmap, _rotationAngle.value)
            _resultText.value = ocr.text
            _isRecognizing.value = false
        }
    }

    fun clear() {
        _selectedBitmap.value = null
        _rotationAngle.value = 0
        _resultText.value = ""
    }
}
