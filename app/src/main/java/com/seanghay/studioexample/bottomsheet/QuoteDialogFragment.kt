package com.seanghay.studioexample.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.seanghay.studioexample.FontFamilyAdapter
import com.seanghay.studioexample.FontLoader
import com.seanghay.studioexample.R
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.android.synthetic.main.dialog_quote.*
import kotlinx.android.synthetic.main.dialog_quote.view.*


class QuoteDialogFragment: AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_quote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.inflateMenu(R.menu.quote)
        toolbar.setNavigationOnClickListener { dismiss() }

        val fontLoader = FontLoader(requireContext().assets)
        val fonts = fontLoader.getFonts()

        val adapter = FontFamilyAdapter(requireContext(), null, fonts)

        val editTextFilledExposedDropdown = view.findViewById<AutoCompleteTextView>(R.id.fontFamily)
        editTextFilledExposedDropdown.setAdapter(adapter)
        editTextFilledExposedDropdown.isEnabled = true
        editTextFilledExposedDropdown.setOnItemClickListener { adapterView, view, i, l ->
           val font = fonts[i]
        }

        chooseColor.setOnClickListener {
            showColorPicker()
        }
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle("Color Picker")
            .setPreferenceName("pref-color")
            .setPositiveButton("Confirm", object: ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    if (envelope == null) return
                    colorPicker.setBackgroundColor(envelope.color)
                }
            })
            .setNegativeButton("Cancel") {dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .show()
    }



    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): QuoteDialogFragment {
            return QuoteDialogFragment()
        }
    }
}