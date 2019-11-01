package com.seanghay.studioexample.experiment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.seanghay.studioexample.R
import com.seanghay.studioexample.experiment.fragment.StudioFragment


class ExperimentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiment)

        if (savedInstanceState == null) {
            val fragment = StudioFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.containerView, fragment, "studio")
                .setPrimaryNavigationFragment(fragment)
                .commitNow()

        }
    }
}