package com.app.clipsteronline.upload.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.clipsteronline.R
import com.app.clipsteronline.upload.adapter.ReelsGalleryAdapter
import com.app.clipsteronline.upload.editor.app.EditorActivity
import com.app.clipsteronline.upload.model.ReelsMediaModel

class UploadReelsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var nextBtn: Button
    private lateinit var adapter: ReelsGalleryAdapter

    private val mediaList = ArrayList<ReelsMediaModel>()
    private val selectedMedia = ArrayList<String>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) loadGallery() else toast("Gallery permission denied")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.activity_upload_reels, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        nextBtn = view.findViewById(R.id.nextBtn)

        setupRecycler()
        setupClicks()
        checkPermission()
        updateNextButton()
        return view
    }

    private fun setupClicks() {
        nextBtn.setOnClickListener {
            if (selectedMedia.isEmpty()) {
                toast("Select at least 1 media")
                return@setOnClickListener
            }
            val intent = Intent(requireContext(), EditorActivity::class.java).apply {
                putStringArrayListExtra(EditorActivity.EXTRA_SELECTED_MEDIA, ArrayList(selectedMedia))
            }
            startActivity(intent)
        }
    }

    private fun setupRecycler() {
        adapter = ReelsGalleryAdapter(mediaList, selectedMedia) { media ->
            val key = media.path
            if (selectedMedia.contains(key)) {
                selectedMedia.remove(key)
            } else {
                if (selectedMedia.size >= 10) {
                    toast("Maximum 10 items allowed")
                    return@ReelsGalleryAdapter
                }
                selectedMedia.add(key)
            }
            updateNextButton()
            adapter.notifyDataSetChanged()
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
        recyclerView.setItemViewCacheSize(35)
        recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        recyclerView.clipToPadding = false
        recyclerView.adapter = adapter
    }

    private fun updateNextButton() {
        nextBtn.visibility = if (selectedMedia.isEmpty()) View.GONE else View.VISIBLE
        nextBtn.text = "Next (${selectedMedia.size})"
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val img = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
            val vid = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO)
            if (img == PackageManager.PERMISSION_GRANTED && vid == PackageManager.PERMISSION_GRANTED) loadGallery()
            else permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO))
        } else {
            val storage = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            if (storage == PackageManager.PERMISSION_GRANTED) loadGallery()
            else permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun loadGallery() {
        mediaList.clear()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Video.Media.DURATION,
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"

        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Files.getContentUri("external"), projection, selection, null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
        )

        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val mediaType = c.getInt(typeCol)
                val isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val durationSec = (c.getLong(durationCol) / 1000).coerceAtLeast(0)
                if (isVideo && durationSec > 120) continue

                val uri: Uri = if (isVideo) {
                    Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                } else {
                    Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                }

                mediaList.add(ReelsMediaModel(path = uri.toString(), isVideo = isVideo, duration = durationSec))
            }
        }

        adapter.notifyDataSetChanged()
        if (mediaList.isEmpty()) toast("No media found")
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
