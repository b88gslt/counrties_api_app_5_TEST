package com.example.hm_third_count

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(
            cl,
            dagger.hilt.android.testing.HiltTestApplication::class.java.name,
            context
        )
    }
}
