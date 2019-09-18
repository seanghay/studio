package com.seanghay.studioexample.bottomsheet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.seanghay.studioexample.FontFamilyAdapter
import com.seanghay.studioexample.FontLoader
import com.seanghay.studioexample.R
import com.seanghay.studioexample.sticker.QuoteState
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.android.synthetic.main.dialog_quote.*
import java.lang.NullPointerException
import kotlin.math.roundToInt


class QuoteDialogFragment : AppCompatDialogFragment() {

    private var listener: QuoteListener? = null

    private var state: QuoteState? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is QuoteListener) {
            this.listener = context
        }
    }

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

    private var bgColor = Color.WHITE

    private var currentFont: FontLoader.FontFamily? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        state = arguments?.getParcelable(KEY_STATE) ?: throw NullPointerException("State was null")



        toolbar.inflateMenu(R.menu.quote)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.toggle_bg) toggleBg()
            if (it.itemId == R.id.save) saveBitmap()
            false
        }

        val fontLoader = FontLoader(requireContext().assets)
        val fonts = fontLoader.getFonts()

        val adapter = FontFamilyAdapter(requireContext(), null, fonts)

        val editTextFilledExposedDropdown = view.findViewById<AutoCompleteTextView>(R.id.fontFamily)
        editTextFilledExposedDropdown.setAdapter(adapter)
        editTextFilledExposedDropdown.isEnabled = true
        editTextFilledExposedDropdown.setOnItemClickListener { adapterView, view, i, l ->
            currentFont = fonts[i]
            preview.setTypeface(currentFont!!.getTypeface(requireContext().assets))
        }

        chooseColor.setOnClickListener {
            showColorPicker()
        }

        editText.addTextChangedListener {
            preview.setText(it.toString())
        }

        val defaultSize = 12f.dip()

        seekBarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val size = defaultSize + p1.dipF()
                preview.setTextSize(size)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        preview.setBackgroundColor(bgColor)
        state?.applyTo(requireContext().assets, preview)

        state?.textColor?.let {
            colorPicker.setBackgroundColor(it)
        }

        state?.text.let {
            editText.setText(it)
        }

        state?.fontFamily?.let {
            currentFont = it
        }
    }


    private fun saveBitmap() {
        listener?.onReceiveQuoteBitmap(preview.getBitmap())
        listener?.newQuoteState(QuoteState.from(preview, currentFont))
        dismiss()
    }


    private fun toggleBg() {
        bgColor = if (bgColor == Color.WHITE) Color.DKGRAY
        else Color.WHITE
        preview.setBackgroundColor(bgColor)
    }


    private fun Float.px(): Float {
        return this / resources.displayMetrics.density
    }


    @Px
    private fun Int.dip(): Int {
        return dipF().roundToInt()
    }

    @Px
    private fun Int.dipF(): Float {
        return toFloat().dip()
    }

    @Px
    private fun Float.dip(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(requireContext())
            .setTitle("Color Picker")
            .setPreferenceName("pref-color")
            .setPositiveButton("Confirm", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    if (envelope == null) return
                    colorPicker.setBackgroundColor(envelope.color)
                    preview.setTextColor(envelope.color)
                }
            })
            .setNegativeButton("Cancel") { dialogInterface, i ->
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

    interface QuoteListener {
        fun onReceiveQuoteBitmap(bitmap: Bitmap)
        fun newQuoteState(quoteState: QuoteState)
    }

    companion object {
        private const val KEY_STATE = "quote-state"

        @JvmStatic
        fun newInstance(state: QuoteState): QuoteDialogFragment {
            val args = Bundle()
            args.putParcelable(KEY_STATE, state)
            return QuoteDialogFragment().also {
                it.arguments = args
            }
        }
    }
}