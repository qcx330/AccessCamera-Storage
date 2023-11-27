package com.example.accesscamerastore

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {
    // Define the button and imageview type variable
    private lateinit var cameraOpenId: Button
    private lateinit var btnRotate: Button
    lateinit var clickImageId: ImageView
    val APP_TAG = "MyCustomApp"
    lateinit var photoFileName :String
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraOpenId = findViewById(R.id.camera_button)
        btnRotate = findViewById(R.id.rotate_button)
        clickImageId = findViewById(R.id.click_image)

        cameraOpenId.setOnClickListener {
            val camera_intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            photoFileName = "FOTO_$timeStamp.jpg"
            photoFile = getPhotoFileUri(photoFileName)
            Log.d(APP_TAG, photoFile.toString())
            if (photoFile != null) {
                val fileProvider: Uri =
                    FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile!!
                    )
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
                if (camera_intent.resolveActivity(packageManager) != null) {
                    // Start the image capture intent to take photo
                    startActivityForResult(camera_intent, pic_id)
                }
            }
        }

        btnRotate.setOnClickListener(){
            clickImageId.setImageBitmap(rotateBitmapOrientation(photoFile.toString()))

        }
    }

    // Returns the File for a photo stored on disk given the fileName
    fun getPhotoFileUri(fileName: String): File {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pic_id) {
            val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
            clickImageId.setImageBitmap(takenImage)
        }
    }
    fun rotateBitmapOrientation(photoFilePath: String?): Bitmap? {
        // Create and configure BitmapFactory
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoFilePath, bounds)
        val opts = BitmapFactory.Options()
        val bm = BitmapFactory.decodeFile(photoFilePath, opts)
        // Read EXIF Data
        var exif: ExifInterface? = null
        try {
            exif = ExifInterface(photoFilePath!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientString = exif!!.getAttribute(ExifInterface.TAG_ORIENTATION)
        val orientation =
            orientString?.toInt() ?: ExifInterface.ORIENTATION_NORMAL
        var rotationAngle = 0
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270
        // Rotate Bitmap
        val matrix = Matrix()
        matrix.setRotate(rotationAngle.toFloat(), bm.width.toFloat() / 2, bm.height.toFloat() / 2)
        // Return result
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true)
    }
    companion object {
        // Define the pic id
        private const val pic_id = 123
    }
}
