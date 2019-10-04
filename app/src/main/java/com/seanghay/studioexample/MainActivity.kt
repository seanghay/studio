package com.seanghay.studioexample

import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.TextureView
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.room.Room
import com.seanghay.studio.gles.shader.filter.pack.PackFilter
import com.seanghay.studio.utils.BitmapProcessor
import com.seanghay.studioexample.bottomsheet.FilterPackDialogFragment
import com.seanghay.studioexample.bottomsheet.QuoteDialogFragment
import com.seanghay.studioexample.sticker.QuoteState
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity(), FilterPackDialogFragment.FilterPackListener,
    QuoteDialogFragment.QuoteListener {


    private val slides = arrayListOf<SlideEntity>()
    private val slideAdapter: SlideAdapter = SlideAdapter(slides)
    private lateinit var appDatabase: AppDatabase

    private var audio: AudioEntity? = null

    private val isLoading = MutableLiveData<Boolean>()
    private lateinit var compressor: Compressor
    private lateinit var composer: VideoComposer
    private val transitionAdapter: TransitionsAdapter = TransitionsAdapter(arrayListOf())

    private lateinit var quoteState: QuoteState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        quoteState = QuoteState(
            text = "Hello, World!",
            textColor = Color.BLACK,
            textSize = 18f.dip(resources),
            scaleFactor = 1f,
            fontFamily = null
        )

        composer = VideoComposer(this)
        compressor = Compressor(this)
        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, "app-v1")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        setupStatusBar(Color.parseColor("#80FFFFFF"))
        setLightStatusBar(true)
        setContentView(R.layout.activity_main)

        isLoading.value = true
        isLoading.observe(this, Observer {
            loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        })
        initToolbar()
        initTransitions()
        setEvents()
        initPhotos()
        initAudio()
        initProgress()
        initRendering()

        isLoading.value = false

        launch()
    }

    private fun launch() {
        var littleBox: LittleBox? = null

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                littleBox?.resize(width, height)
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                // littleBox?.release()
                return false
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                if (surface == null) return
                littleBox = LittleBox(this@MainActivity, surface, width, height)
                littleBox?.setComposer(composer)

                seekBarProgress.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        textViewProgress.text = HtmlCompat.fromHtml(
                            "Progress: <strong>$p1%</strong>",
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )

                        val progress = p1.toFloat() / p0!!.max.toFloat()
                        if (p2) composer.renderAtProgress(progress)

                        littleBox?.draw()
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                        timeAnimator.cancel()
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }
                })

                buttonExport.setOnClickListener {
                    val path = File(externalCacheDir, "my-video.mp4").path
                    littleBox?.exportToVideo(path)
                }
            }
        }


    }

    override fun newQuoteState(quoteState: QuoteState) {
        this.quoteState = quoteState
    }

    override fun onReceiveQuoteBitmap(bitmap: Bitmap) {
        composer.applyQuoteBitmap(bitmap)
    }


    override fun onFilterPackSaved(filterPack: PackFilter) {
        if (slideAdapter.selectedAt != -1) {
            composer.getScenes()[slideAdapter.selectedAt].filter = filterPack
        } else {
            composer.applyFilterPack(filterPack, true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.filter) {
            var filter = composer.getCurrentFilterPack()

            if (slideAdapter.selectedAt != -1) {
                filter = composer.getScenes()[slideAdapter.selectedAt].filter
            }

            FilterPackDialogFragment
                .newInstance(filter)
                .show(supportFragmentManager, "filters")
        }

        if (item.itemId == R.id.quote) {
            QuoteDialogFragment.newInstance(quoteState)
                .show(supportFragmentManager, "quote")
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun initTransitions() {
        val transitions = composer.getTransitions()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        transitionAdapter.items = transitions

        recyclerViewTransitions.let {
            it.adapter = transitionAdapter
            it.layoutManager = layoutManager
            it.setHasFixedSize(true)
            (it.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        transitionAdapter.selectionChange = {
            val transition = transitions[transitionAdapter.selectedAt]
            val scene = composer.getScenes().getOrNull(slideAdapter.selectedAt)
            if (scene != null) {
                scene.transition = transition
                recyclerViewTransitions.smoothScrollToPosition(transitionAdapter.selectedAt)
            }
        }

    }

    private fun initRendering() {
        // textureView.surfaceTextureListener = composer.surfaceTextureListener
    }


    private fun initAudio() {
        if (audio != null) return
        val audioDb = appDatabase.audioDao().first() ?: return
        setAudio(audioDb)
    }

    private fun initProgress() {
//        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                textViewProgress.text = HtmlCompat.fromHtml(
//                    "Progress: <strong>$p1%</strong>",
//                    HtmlCompat.FROM_HTML_MODE_LEGACY
//                )
//
//                val progress = p1.toFloat() / p0!!.max.toFloat()
//                if (p2)
//                    composer.progress = progress
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//                timeAnimator.cancel()
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//
//            }
//        })
    }

    private fun initPhotos() {
        if (slides.isEmpty()) {
            val fromDb = appDatabase.slideDao().getAll()
            if (fromDb.isNotEmpty()) slides.addAll(fromDb)
        }


        recyclerView.let {
            it.adapter = slideAdapter
            it.setHasFixedSize(true)
            (it.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        val bitmaps = slides.map { it.path to BitmapProcessor.load(it.path) }.toTypedArray()
        composer.insertScenes(*bitmaps)

        slideAdapter.selectionChange = {
            val sceneIndex = slideAdapter.selectedAt
            val transition = composer.getScenes().get(sceneIndex).transition
            val selectedTransition =
                composer.getTransitions().firstOrNull { it.name == transition.name }
            if (selectedTransition != null) {
                val indexOf = composer.getTransitions().indexOf(selectedTransition)
                transitionAdapter.select(indexOf)
                recyclerViewTransitions.smoothScrollToPosition(indexOf)
            }
        }
    }

    private lateinit var timeAnimator: ValueAnimator

    private fun setEvents() {
        buttonChoose.setOnClickListener { choosePhotos() }
        buttonChooseAudio.setOnClickListener { chooseAudio() }
        // buttonExport.setOnClickListener { exportAsVideoFile() }
        buttonSaveDraft.setOnClickListener { saveDraft() }
        buttonResetDraft.setOnClickListener { resetDraft() }


        timeAnimator = TimeAnimator.ofInt(0, 10_000)

        timeAnimator.setDuration(50000)
        timeAnimator.interpolator = LinearInterpolator()
        timeAnimator.addUpdateListener {
            seekBarProgress.progress = (it.animatedFraction * seekBarProgress.max).toInt()
            composer.progress = it.animatedFraction

        }

        timeAnimator.repeatMode = TimeAnimator.RESTART
        timeAnimator.repeatCount = TimeAnimator.INFINITE

        imageButtonControl.setOnClickListener {

            if (timeAnimator.isPaused) {
                timeAnimator.resume()
                return@setOnClickListener
            }

            if (!timeAnimator.isStarted) {
                timeAnimator.start()
                return@setOnClickListener
            }

            if (timeAnimator.isStarted) {
                timeAnimator.pause()
                return@setOnClickListener
            }
        }
    }

    private fun resetDraft() {
        appDatabase.slideDao().deleteAll()
        appDatabase.audioDao().deleteAll()
        Toast.makeText(this, "Draft Reset", Toast.LENGTH_SHORT).show()
    }

    private fun saveDraft() {
        if (slides.isEmpty()) return
        appDatabase.slideDao().upsert(*slides.toTypedArray())
        if (audio != null) appDatabase.audioDao().upsert(audio!!)
        Toast.makeText(this, "Draft Saved", Toast.LENGTH_SHORT).show()
    }

    private fun exportAsVideoFile() {
//        thread {
//            val c = composer
//            val path = File(externalCacheDir, "my-video.mp4").path
//            val mp4Composer = Mp4Composer(c.renderThread()?.getEglCore(), c, path,  composer.totalDuration)
//            c.videoSize = Size(mp4Composer.width, mp4Composer.height)
//            c.width = mp4Composer.width
//            c.height = mp4Composer.height
//
//            mp4Composer.start()
//        }


    }

    private fun chooseAudio() {
        val audioPickerIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(audioPickerIntent, 1)
    }

    private fun choosePhotos() {
        Matisse.from(this)
            .choose(MimeType.ofImage())
            .maxSelectable(30)
            .theme(R.style.Matisse_Dracula)
            .countable(true)
            .imageEngine(PicassoEngine())
            .forResult(0)
    }


    override fun onDestroy() {
        super.onDestroy()
        composer.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val items = Matisse.obtainResult(data)
            applyData(items)
        }

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val audioFile = data?.data ?: return

            val inputStream = contentResolver.openInputStream(audioFile) ?: return
            val cursor = contentResolver.query(audioFile, null, null, null, null)
            val nameColumn = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val name = cursor.getString(nameColumn)

            val outputFile = File(externalCacheDir, "audio-${UUID.randomUUID()}" + name)
            val fileOutputStream = FileOutputStream(outputFile)
            IOUtils.copy(inputStream, fileOutputStream)
            inputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()

            val audio = AudioEntity(path = outputFile.path)
            setAudio(audio)
        }
    }

    private fun setAudio(audioEntity: AudioEntity) {
        this.audio = audioEntity
        textViewAudio.text = HtmlCompat.fromHtml(
            "Audio name: <strong>${audioEntity!!.path}</string>",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    private fun applyData(items: List<Uri>) {
        slides.addAll(items.map {
            val inputStream = contentResolver.openInputStream(it)
            val file = File.createTempFile("filename", null, cacheDir)
            val fileOutputStream = FileOutputStream(file)
            IOUtils.copy(inputStream, fileOutputStream)
            val compressedFile = compressor.setQuality(40)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .compressToFile(file, "photos-${UUID.randomUUID()}.jpg")

            SlideEntity(compressedFile.path)
        })

        slideAdapter.notifyDataSetChanged()
    }


    private fun play(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        intent.setDataAndType(Uri.parse(path), "video/mp4")
        startActivity(intent)
    }

    private fun setupStatusBar(color: Int) {
        window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = color
    }

    private fun setLightStatusBar(light: Boolean = true) {
        if (light) {
            window.decorView.systemUiVisibility =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.decorView.systemUiVisibility =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                else View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

}




