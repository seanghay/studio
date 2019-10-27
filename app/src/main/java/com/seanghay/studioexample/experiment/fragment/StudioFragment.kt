package com.seanghay.studioexample.experiment.fragment

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.seanghay.studioexample.R
import com.seanghay.studioexample.dao.md5
import com.seanghay.studioexample.experiment.core.EditorEngine
import com.seanghay.studioexample.experiment.viewmodel.StudioViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_studio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StudioFragment : Fragment() {

    private lateinit var viewModel: StudioViewModel

    private lateinit var textureView: TextureView
    private val editorEngine = EditorEngine.getInstance()

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(viewModelStore, ViewModelProvider.NewInstanceFactory()).get(
            StudioViewModel::class.java
        )
    }


    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            editorEngine.setViewportSize(width, height)
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            // ignored
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            editorEngine.release()
            compositeDisposable.clear()
            return true // will be release automatically
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            editorEngine.initialize()
            editorEngine.setViewportSize(width, height)
            editorEngine.attachSurfaceTexture(surface)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_studio, container, false).apply {
            textureView = findViewById(R.id.textureView)
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editorEngine.getFrameRateFlowable()
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { textViewInfo.setText("Initializing...") }
            .subscribeBy {
                textViewInfo.setText("Frame rate: $it fps")
            }
            .addTo(compositeDisposable)

        buttonAttach.setOnClickListener { choosePhotos() }

        Transformations.map(viewModel.isLoading) {
            if (it) View.VISIBLE else View.GONE
        }.observe(this, Observer(isLoading::setVisibility))

    }

    private suspend fun requestRender() {
        withContext(Dispatchers.IO) {
            viewModel.isLoading.postValue(true)

            val compressDir = File(requireContext().externalCacheDir, "images")
            if (!compressDir.exists()) compressDir.mkdirs()
            val bitmaps = viewModel.photos.map { it.toString().md5() to it.toBitmapOrNull() }
            val files = mutableListOf<String>()

            for (bitmap in bitmaps) {
                val file = File(compressDir, bitmap.first + ".jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use { bitmap.second?.compress(Bitmap.CompressFormat.JPEG, 10, it) }
                files.add(file.path)
            }

            viewModel.compressedPhotos.addAll(files)
            launch(context = Dispatchers.Main) {
                Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show()
                viewModel.isLoading.value = false
                updateRenderer()
            }
        }
    }

    private fun updateRenderer() {
        editorEngine.attachBitmaps(viewModel.compressedPhotos)
    }


    private fun choosePhotos() {
        if (!permissions.all { it.isGranted() }) {
            requestPermissions(permissions, REQUEST_CODE_CHOOSE_PHOTOS)
            return
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val chooserIntent = Intent.createChooser(intent, "Choose images")
        startActivityForResult(chooserIntent, REQUEST_CODE_CHOOSE_GALLERY)
    }


    private fun Uri.toBitmapOrNull(): Bitmap? {
        return try {
            toBitmap()
        } catch (ex: IOException) {
            null
        }
    }

    @Suppress("DEPRECATION")
    @Throws(IOException::class)
    private fun Uri.toBitmap(): Bitmap {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, this)
        } else {
            val source = ImageDecoder.createSource(requireContext().contentResolver, this)
            ImageDecoder.decodeBitmap(source)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == REQUEST_CODE_CHOOSE_GALLERY) {
            if (data == null) return

            val list = arrayListOf<Uri>()

            val clipData = data.clipData
            val imageUri = data.data

            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    val uri = item.uri
                    list.add(uri)
                }
            } else {
                if (imageUri != null) {
                    list.add(imageUri)
                }
            }

            if (list.isNotEmpty()) {
                viewModel.photos.addAll(list)

                lifecycleScope.launch { requestRender() }

            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CHOOSE_PHOTOS && grantResults.all { it.isGranted() }) {
            choosePhotos()
        } else Toast.makeText(context, "Permission required", Toast.LENGTH_SHORT).show()
    }

    private fun String.isGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            this
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun Int.isGranted(): Boolean {
        return this == PackageManager.PERMISSION_GRANTED
    }


    companion object {

        private const val REQUEST_CODE_CHOOSE_PHOTOS = 0
        private const val REQUEST_CODE_CHOOSE_GALLERY = 1

        private const val TAG = "StudioFragment"

        fun logd(message: Any) = Log.d(TAG, message.toString())

        @JvmStatic
        private val permissions = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)

        @JvmStatic
        fun newInstance(): StudioFragment {
            return StudioFragment()
        }
    }
}