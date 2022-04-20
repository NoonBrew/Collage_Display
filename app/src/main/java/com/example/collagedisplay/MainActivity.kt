package com.example.collagedisplay


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {

//    private lateinit var mainImageButton: ImageButton
    private lateinit var imageButtons: List<ImageButton>

    private lateinit var mainView: View

//    private var newImagePath: String? = null
//    private var visibleImagePath: String? = null
    private var imagePaths: ArrayList<String?> = arrayListOf(null, null, null, null)

    private var imageIndexHolder: Int? = null // Index of what image button they are using.

    private var currentImagePath: String? = null // Current image path the user is working with.

    private val IMAGE_PATH_LIST_ARRAY_KEY = "image path list key"
    private val IMAGE_INDEX_KEY = "image index key"
    private val CURRENT_IMAGE_PATH_KEY = "current image path key"

    private val cameraActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result -> handleImage(result)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageIndexHolder = savedInstanceState?.getInt(IMAGE_INDEX_KEY)
        currentImagePath = savedInstanceState?.getString(CURRENT_IMAGE_PATH_KEY)
        imagePaths = savedInstanceState?.getStringArrayList(IMAGE_PATH_LIST_ARRAY_KEY)
            ?: arrayListOf(null, null, null, null)

        mainView = findViewById(R.id.content_layout)

        imageButtons = listOf<ImageButton>(
            findViewById(R.id.main_image_button),
            findViewById(R.id.main_image_button2),
            findViewById(R.id.main_image_button3),
            findViewById(R.id.main_image_button4)
        )

//        mainImageButton = findViewById(R.id.main_image_button)
//
//        mainImageButton.setOnClickListener{
//            takePicture()
//        }

        for (imageButton in imageButtons) {
            imageButton.setOnClickListener { ib ->
                takePictureFor(ib as ImageButton) // as tells the function that we are passing an ImageButton
            }
        }
    }

    // Sends the the image paths as an outState bundle to be read on View being recreated.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(IMAGE_PATH_LIST_ARRAY_KEY, imagePaths)
        outState.putString(CURRENT_IMAGE_PATH_KEY, currentImagePath)
        imageIndexHolder?.let { index -> outState.putInt(IMAGE_INDEX_KEY, index) }

    }

    private fun takePictureFor(imageButton: ImageButton) {

        val index = imageButtons.indexOf(imageButton) // gets the reference to the button
        imageIndexHolder = index // and passes that to our imageIndexHolder

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val (imageFile, imageFilePath) = createImageFile()
        if (imageFile != null) {
            // This is a refrence to the image file.
            currentImagePath = imageFilePath
            val imageUri = FileProvider.getUriForFile(this,
                "com.example.collagedisplay.fileprovider",
                imageFile) // Creates a URI for the new image file that was created.

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            // We now create a file to write the image to and store the location of where that file is on the device
            // and then provide that information as an extra to the native camera app.
            cameraActivityLauncher.launch(takePictureIntent)
        }

    }

    private fun createImageFile(): Pair<File?, String?>{
        try {
            val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) // Used to create a unique file name
            val imageFileName = "COLLAGE_$dateTime" // Adds our unique timeStamp to the photo name.
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) // Checks the default image storage
            val file = File.createTempFile(imageFileName, ".jpg", storageDir)
            val filePath = file.absolutePath

            return file to filePath // a refrence to the file and the string repersentation of the files location.

        } catch (ex: IOException) {
            return null to null
        }
    }

    private fun handleImage(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                Log.d(TAG, "Result ok, user took a picture, image at $currentImagePath")
//                visibleImagePath = newImagePath  //Runs once UI is set up and checks ImagePath if it has an image
                // Stores the current image path with the index of the button pressed
                imageIndexHolder?.let { index -> imagePaths[index] = currentImagePath }
            }
            RESULT_CANCELED -> {
                Log.d(TAG, "Result cancelled, no picture taken")
            }
        }
    }

    // OnWindowChanged is a callback function that is called when a device is rotated
    // when the user interface has been completely inflated, and gets called when returing from
    // another activity.
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "on window focus changed $hasFocus visible image at $currentImagePath")
        if (hasFocus) {
//            visibleImagePath?.let { imagePath ->
//                loadImage(mainImageButton, imagePath)
//            }
            imageButtons.zip(imagePaths) { imageButton, imagePath ->
                imagePath?.let {
                    loadImage(imageButton, imagePath)
                }
            }
        }
    }

    // loads an image path into an image button.
    private fun loadImage(imageButton: ImageButton, imagePath: String) {
        Picasso.get()
            .load(File(imagePath))
            .error(android.R.drawable.stat_notify_error)
            .fit()
            .centerCrop()
            .into(imageButton, object: Callback {
                override fun onSuccess() {
                    Log.d(TAG, "loaded image $imagePath")
                }

                override fun onError(e: Exception?) {
                    Log.e(TAG, "Error loading image $imagePath", e)
                }
            })
    }
}