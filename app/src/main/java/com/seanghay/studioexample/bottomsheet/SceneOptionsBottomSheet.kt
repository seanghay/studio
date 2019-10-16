package com.seanghay.studioexample.bottomsheet

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.FloatRange
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seanghay.studioexample.R
import kotlinx.android.synthetic.main.fragment_scene_option.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class SceneOptionsBottomSheet : BottomSheetDialogFragment() {

    private var state: OptionState? = null
    private var listener: SceneOptionStateListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SceneOptionStateListener)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        state =
            arguments?.getParcelable("state") ?: throw RuntimeException("There is no initial state")
        return inflater.inflate(R.layout.fragment_scene_option, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setState()
        updateDuration()

        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateDuration()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        buttonSave.setOnClickListener {
            saveState()
        }

        buttonDelete.setOnClickListener {
            listener?.onStateChange(state?.copy(delete = true)!!)
            dismiss()
        }
    }


    private fun saveState() {
        this.state?.let {
            listener?.onStateChange(
                it.copy(
                    blur = checkboxBlur.isChecked,
                    crop = currentCrop()
                )
            )
        }
        dismiss()
    }

    private fun currentCrop(): String {
        if (fitCenter.isChecked) return "fit-center"
        if (fitEnd.isChecked) return "fit-end"
        if (fitStart.isChecked) return "fit-start"
        if (fillCenter.isChecked) return "fill-center"
        if (fillEnd.isChecked) return "fill-end"
        if (fillStart.isChecked) return "fill-start"
        return "fit-center"
    }

    private fun setState() {
        state?.let { s ->
            seekBarDuration.progress = calcProgress(s.duration)
            groupfill.check(getCheckedId(s.crop!!))

        }
    }

    @IdRes
    private fun getCheckedId(key: String): Int {
        return when (key) {
            "fit-center" -> R.id.fitCenter
            "fit-end" -> R.id.fitEnd
            "fit-start" -> R.id.fitStart
            "fill-center" -> R.id.fillCenter
            "fill-end" -> R.id.fillEnd
            "fill-start" -> R.id.fillStart
            else -> R.id.fitCenter
        }
    }


    private fun updateDuration() {
        val d =
            calculateDuration(seekBarDuration.progress.toFloat() / seekBarDuration.max.toFloat())
        duration.text = formatDuration(d)
        this.state?.duration = d
    }


    private fun formatDuration(millis: Long): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    millis
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    millis
                )
            )
        )
    }

    companion object {

        private const val MAX_DURATION = 20 * 1000L // 10 seconds
        private const val MIN_DURATION = 2 * 1000L // 2 seconds

        fun calculateDuration(@FloatRange(from = 0.0, to = 1.0) progress: Float): Long {
            return max((MAX_DURATION * progress).toLong(), MIN_DURATION)
        }

        fun calcProgress(duration: Long): Int {
            val delta = MAX_DURATION - MIN_DURATION
            return ((duration.toFloat() / delta.toFloat()) * 100f).toInt()
        }


        @JvmStatic
        fun newInstance(state: OptionState): SceneOptionsBottomSheet {
            return SceneOptionsBottomSheet().apply {
                arguments = bundleOf("state" to state)
            }
        }
    }


    interface SceneOptionStateListener {
        fun onStateChange(state: OptionState)
    }

    data class OptionState(
        var id: String,
        var duration: Long,
        var crop: String? = "fit-center",
        var blur: Boolean = true,
        var delete: Boolean = false
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readLong(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeLong(duration)
            parcel.writeString(crop)
            parcel.writeByte(if (blur) 1 else 0)
            parcel.writeByte(if (delete) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<OptionState> {
            override fun createFromParcel(parcel: Parcel): OptionState {
                return OptionState(parcel)
            }

            override fun newArray(size: Int): Array<OptionState?> {
                return arrayOfNulls(size)
            }
        }
    }
}