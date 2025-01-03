package com.example.mycallapp.commons.base

import androidx.appcompat.app.AppCompatActivity
import com.example.mycallapp.commons.events.SingleLiveEvent
import com.example.mycallapp.commons.events.UiEvent

abstract class BaseActivity : AppCompatActivity() {

    /**
     * Event that can be received in every activity that extends [BaseActivity]
     */
    val uiEvent = SingleLiveEvent<UiEvent>()

}