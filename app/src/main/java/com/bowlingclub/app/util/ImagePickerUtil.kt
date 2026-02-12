package com.bowlingclub.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImagePickerUtil {

    /**
     * 갤러리에서 선택한 이미지를 앱 내부 저장소에 복사 및 압축
     * @param context Context
     * @param uri 갤러리 이미지 URI
     * @return 저장된 파일의 절대 경로 (실패 시 null)
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            val resizedBitmap = resizeBitmap(bitmap, 512, 512)
            val profileDir = File(context.filesDir, "profiles")
            if (!profileDir.exists()) profileDir.mkdirs()

            val filename = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(profileDir, filename)

            FileOutputStream(file).use { fos ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                fos.flush()
            }

            resizedBitmap.recycle()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 프로필 이미지 파일 삭제
     * @param imagePath 이미지 파일 경로
     * @return 삭제 성공 여부
     */
    fun deleteProfileImage(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            file.exists() && file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 경로에서 File 객체 반환
     * @param imagePath 이미지 파일 경로
     * @return File 객체 (존재하면) 또는 null
     */
    fun getProfileImageFile(imagePath: String): File? {
        return try {
            val file = File(imagePath)
            if (file.exists()) file else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Bitmap 크기 조정
     * @param bitmap 원본 Bitmap
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @return 크기 조정된 Bitmap
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = if (width > height) {
            maxWidth.toFloat() / width
        } else {
            maxHeight.toFloat() / height
        }

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
