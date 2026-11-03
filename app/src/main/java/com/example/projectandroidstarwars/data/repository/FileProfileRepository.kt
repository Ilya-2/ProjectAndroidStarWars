package com.example.projectandroidstarwars.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.AtomicFile
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.projectandroidstarwars.domain.model.UserProfile
import com.example.projectandroidstarwars.domain.repository.ProfileException
import com.example.projectandroidstarwars.domain.repository.ProfileRepository
import java.io.DataInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FileProfileRepository(
    context: Context
) : ProfileRepository {

    private val appContext = context.applicationContext

    private val profileFile = AtomicFile(
        File(appContext.filesDir, "user_profile.json")
    )

    private val profileMutex = Mutex()

    override suspend fun load(): UserProfile =
        withContext(Dispatchers.IO) {
            profileMutex.withLock {
                val text = try {
                    profileFile.openRead()
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readText() }
                } catch (_: FileNotFoundException) {
                    return@withLock UserProfile()
                }

                val json = JSONObject(text)

                UserProfile(
                    fullName = json.optString("fullName", ""),
                    avatarUri = json.optString("avatarUri", ""),
                    resumeUrl = json.optString("resumeUrl", ""),
                    favoriteLessonTime = json.optString(
                        "favoriteLessonTime",
                        ""
                    )
                )
            }
        }

    override suspend fun save(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            profileMutex.withLock {
                val json = JSONObject()
                    .put("fullName", profile.fullName)
                    .put("avatarUri", profile.avatarUri)
                    .put("resumeUrl", profile.resumeUrl)
                    .put(
                        "favoriteLessonTime",
                        profile.favoriteLessonTime
                    )

                val output = profileFile.startWrite()

                try {
                    output.write(
                        json.toString().toByteArray(Charsets.UTF_8)
                    )

                    profileFile.finishWrite(output)
                } catch (error: Throwable) {
                    profileFile.failWrite(output)
                    throw error
                }
            }
        }
    }

    override suspend fun importAvatar(
        sourceUri: String
    ): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(sourceUri)
        val resolver = appContext.contentResolver

        val mimeType = resolver.getType(uri)
        val extension = mimeType
            ?.let {
                MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(it)
            }
            ?: "jpg"

        val directory = getDirectory(
            parent = appContext.filesDir,
            name = "profile_avatars"
        )

        val target = File.createTempFile(
            "avatar_",
            ".$extension",
            directory
        )

        try {
            val input = resolver.openInputStream(uri)
                ?: throw ProfileException(
                    "Не удалось открыть выбранную фотографию."
                )

            input.use { source ->
                target.outputStream().use { destination ->
                    copyLimited(
                        input = source,
                        output = destination,
                        maxBytes = MAX_AVATAR_BYTES
                    )
                }
            }

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(
                target.absolutePath,
                options
            )

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                throw ProfileException(
                    "Выбранный файл не удалось прочитать как изображение."
                )
            }

            contentUri(target)
        } catch (error: Throwable) {
            target.delete()
            throw error
        }
    }

    override suspend fun createCameraUri(): String =
        withContext(Dispatchers.IO) {
            val directory = getDirectory(
                parent = appContext.cacheDir,
                name = "profile_camera"
            )

            val file = File.createTempFile(
                "camera_",
                ".jpg",
                directory
            )

            contentUri(file)
        }

    override suspend fun downloadResume(
        url: String
    ): String = withContext(Dispatchers.IO) {
        val address = URL(url)

        if (!address.protocol.equals("https", ignoreCase = true)) {
            throw ProfileException(
                "Для резюме нужна HTTPS-ссылка."
            )
        }

        val connection = address.openConnection()
                as HttpURLConnection

        connection.apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/pdf")
        }

        var target: File? = null

        try {
            if (connection.responseCode !in 200..299) {
                throw ProfileException(
                    "Не удалось скачать резюме. " +
                            "Код сервера: ${connection.responseCode}."
                )
            }

            val directory = getDirectory(
                parent = appContext.cacheDir,
                name = "profile_documents"
            )

            val file = File.createTempFile(
                "resume_",
                ".pdf",
                directory
            )

            target = file

            connection.inputStream.buffered().use { input ->
                input.mark(8)

                val signature = ByteArray(5)
                DataInputStream(input).readFully(signature)

                if (String(signature, Charsets.US_ASCII) != "%PDF-") {
                    throw ProfileException(
                        "По ссылке находится не PDF-файл. " +
                                "Укажи прямую ссылку на документ."
                    )
                }

                input.reset()

                file.outputStream().use { output ->
                    copyLimited(
                        input = input,
                        output = output,
                        maxBytes = MAX_RESUME_BYTES
                    )
                }
            }

            contentUri(file)
        } catch (error: Throwable) {
            target?.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun contentUri(file: File): String {
        return FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        ).toString()
    }

    private fun getDirectory(
        parent: File,
        name: String
    ): File {
        val directory = File(parent, name)

        if (!directory.isDirectory && !directory.mkdirs()) {
            throw ProfileException(
                "Не удалось создать папку для файлов профиля."
            )
        }

        return directory
    }

    private suspend fun copyLimited(
        input: InputStream,
        output: OutputStream,
        maxBytes: Long
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L

        while (true) {
            coroutineContext.ensureActive()

            val count = input.read(buffer)
            if (count == -1) break

            total += count

            if (total > maxBytes) {
                throw ProfileException(
                    "Файл слишком большой. Максимум: " +
                            "${maxBytes / 1024 / 1024} МБ."
                )
            }

            output.write(buffer, 0, count)
        }

        if (total == 0L) {
            throw ProfileException("Получен пустой файл.")
        }
    }

    private companion object {
        const val MAX_AVATAR_BYTES = 20L * 1024 * 1024
        const val MAX_RESUME_BYTES = 30L * 1024 * 1024
    }
}