package com.seanghay.studioexample.bottomsheet

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.scale
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seanghay.studio.gles.shader.filter.pack.PackFilter
import com.seanghay.studioexample.R
import kotlinx.android.synthetic.main.dialog_filters.*

class FilterPackDialogFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {

    private var listener: FilterPackListener? = null
    private val labelTextViews = mutableMapOf<String, TextView>()
    private var filterPack: PackFilter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterPackListener) {
            this.listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_filters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packFilter = arguments?.getParcelable<PackFilter>(KEY_FILTER_PACK)
            ?: throw NullPointerException("PackFilter was null")
        this.filterPack = packFilter

        traverseLabels(view)
        setValues(filterPack)

        seekBars.forEach {
            val seekBar = view.findViewById<SeekBar>(it.value)
            seekBar.setOnSeekBarChangeListener(this)
        }

        buttonReset {
            this.filterPack = PackFilter()
            setValues(this.filterPack!!)
        }

        buttonSave {
            saveAndClose()
        }
    }

    private fun saveAndClose() {
        listener?.onFilterPackSaved(filterPack!!.copy())
        dismiss()
    }

    private inline operator fun Button.invoke(crossinline invoker: () -> Unit) {
        setOnClickListener { invoker() }
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        if (p0 == null) return
        if (!p2) return

        for (seekBar in seekBars) {
            if (seekBar.value == p0.id) {
                val tag = seekBar.key
                val converter = decodeFunctions[tag]
                packFilterMap[tag]?.set(this.filterPack!!, converter!!(p1.toFloat()))
                setValues(this.filterPack)
            }
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }


    private fun setValues(filterPack: PackFilter?) {
        if (filterPack == null) return
        seekBars.forEach {
            val seekBar = view?.findViewById<SeekBar>(it.value)
            val value = packFilterMap[it.key]?.get(filterPack) ?: 0f

            val converter = encodeFunctions[it.key]
            val progress = converter!!(value)
            seekBar?.progress = progress.toInt()
            val labelConverter = labelFunctions[it.key]

            labelTextViews[it.key]?.text =
                wrap(labels[it.key] ?: "", labelConverter!!(progress).toInt())
        }
    }

    private fun traverseLabels(view: View?) {
        if (view == null) return
        if (view is ViewGroup) view.forEach { traverseLabels(it) }
        else {
            if (view is TextView && view.tag != null) {
                if (view.tag is String) {
                    if (labels.containsKey(view.tag as String)) {
                        labelTextViews[view.tag as String] = view
                    }
                }
            }
        }
    }


    private fun wrap(label: String, progress: Int): CharSequence {
        return SpannableStringBuilder(label)
            .append(" • ")
            .color(Color.BLACK) {
                scale(.8f) {
                    bold {
                        append("$progress%")
                    }
                }
            }
    }


    interface FilterPackListener {
        fun onFilterPackSaved(filterPack: PackFilter)
    }

    companion object {
        private const val KEY_FILTER_PACK = "filterPack"

        private val packFilterMap = mapOf(
            "brightness" to PackFilter::brightness,
            "contrast" to PackFilter::contrast,
            "saturation" to PackFilter::saturation,
            "gamma" to PackFilter::gamma,
            "tint" to PackFilter::tint,
            "warmth" to PackFilter::warmth,
            "sepia" to PackFilter::sepia,
            "vibrant" to PackFilter::vibrant,
            "intensity" to PackFilter::intensity
        )

        private val labels = mapOf(
            "brightness" to "Brightness",
            "contrast" to "Contrast",
            "saturation" to "Saturation",
            "gamma" to "Gamma",
            "tint" to "Tint",
            "warmth" to "Warmth",
            "sepia" to "Sepia",
            "vibrant" to "Vibrant",
            "intensity" to "Intensity"
        )

        private val seekBars = mapOf(
            "brightness" to R.id.brightness,
            "contrast" to R.id.contrast,
            "saturation" to R.id.saturation,
            "gamma" to R.id.gamma,
            "tint" to R.id.tint,
            "warmth" to R.id.warmth,
            "sepia" to R.id.sepia,
            "vibrant" to R.id.vibrant,
            "intensity" to R.id.intensity
        )

        private val encodeFunctions = mapOf<String, ((Float) -> Float)>(
            "brightness" to { value -> (0.5f + value) * 100f },
            "contrast" to { value -> value * 50f },
            "saturation" to { value -> value * 50f },
            "gamma" to { value -> value * 50f },
            "tint" to { value -> (0.5f + value) * 100f },
            "warmth" to { value -> (value + 0.5f) * 100f },
            "sepia" to { value -> value * 100f },
            "vibrant" to { value -> (0.5f + value) * 100f },
            "intensity" to { value -> value * 100f }
        )

        private val decodeFunctions = mapOf<String, ((Float) -> Float)>(
            "brightness" to { value -> (value / 100f) - 0.5f },
            "contrast" to { value -> value / 50f },
            "saturation" to { value -> value / 50f },
            "gamma" to { value -> value / 50f },
            "tint" to { value -> (value / 100f) - 0.5f },
            "warmth" to { value -> (value / 100f) - 0.5f },
            "sepia" to { value -> value / 100f },
            "vibrant" to { value -> (value / 100f) - 0.5f },
            "intensity" to { value -> value / 100f }
        )

        private val labelFunctions = mapOf<String, ((Float) -> Float)>(
            "brightness" to { value -> value - 50f },
            "contrast" to { value -> value - 50f },
            "saturation" to { value -> value - 50f },
            "gamma" to { value -> value - 50f },
            "tint" to { value -> value - 50f },
            "warmth" to { value -> value - 50f },
            "sepia" to { value -> value },
            "vibrant" to { value -> value - 50f },
            "intensity" to { value -> value }
        )


        @JvmStatic
        fun newInstance(filter: PackFilter): FilterPackDialogFragment {
            val fragment = FilterPackDialogFragment()
            val args = Bundle()
            args.putParcelable(KEY_FILTER_PACK, filter)
            fragment.arguments = args
            return fragment
        }
    }
}