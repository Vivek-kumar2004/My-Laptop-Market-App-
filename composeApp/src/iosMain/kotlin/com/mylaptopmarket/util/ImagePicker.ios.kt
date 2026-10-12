package com.mylaptopmarket.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.endEditing
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.sel_registerName
import platform.posix.memcpy

private var retainedPickerDelegate: NSObject? = null

@Composable
actual fun ImagePicker(
    selectionLimit: Int,
    onImagesSelected: (List<ByteArray>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val host = LocalUIViewController.current
    val callback = rememberUpdatedState(onImagesSelected)
    content {
        presentPhotoLibrary(host, selectionLimit) { images ->
            if (images.isNotEmpty()) {
                callback.value(images)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun hideKeyboard() {
    dispatch_async(dispatch_get_main_queue()) {
        val application = UIApplication.sharedApplication
        application.sendAction(
            action = sel_registerName("resignFirstResponder"),
            to = null,
            from = null,
            forEvent = null
        )
        val windows = mutableListOf<UIWindow>()
        application.connectedScenes.forEach { scene ->
            val windowScene = scene as? UIWindowScene ?: return@forEach
            windowScene.windows.forEach { window ->
                (window as? UIWindow)?.let { windows.add(it) }
            }
        }
        if (windows.isEmpty()) {
            application.keyWindow?.let { windows.add(it) }
        }
        windows.forEach { it.endEditing(true) }
    }
}

private fun presentPhotoLibrary(
    preferredHost: UIViewController,
    selectionLimit: Int,
    onPicked: (List<ByteArray>) -> Unit
) {
    dispatch_async(dispatch_get_main_queue()) {
        val host = topViewController() ?: preferredHost
        val configuration = PHPickerConfiguration()
        configuration.selectionLimit = if (selectionLimit <= 0) 0 else selectionLimit.toLong()
        configuration.filter = PHPickerFilter.imagesFilter()
        val picker = PHPickerViewController(configuration = configuration)
        val delegate = MultiPhotoPickerDelegate(onPicked)
        retainedPickerDelegate = delegate
        picker.delegate = delegate
        host.presentViewController(picker, animated = true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class MultiPhotoPickerDelegate(
    private val onPicked: (List<ByteArray>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.mapNotNull { it as? PHPickerResult }
        if (results.isEmpty()) {
            finish(emptyList())
            return
        }

        val collected = arrayOfNulls<ByteArray>(results.size)
        var pending = results.size
        results.forEachIndexed { index, result ->
            val provider = result.itemProvider
            val typeId = listOf("public.heic", "public.jpeg", "public.png", "public.image")
                .firstOrNull { provider.hasItemConformingToTypeIdentifier(it) }
                ?: "public.image"
            provider.loadDataRepresentationForTypeIdentifier(typeId) { data, _ ->
                val nsData = data
                dispatch_async(dispatch_get_main_queue()) {
                    collected[index] = nsData?.let { bytes ->
                        UIImage.imageWithData(bytes)?.scaledForUpload()?.toJpegBytes()
                    }
                    pending -= 1
                    if (pending == 0) {
                        finish(collected.filterNotNull())
                    }
                }
            }
        }
    }

    private fun finish(images: List<ByteArray>) {
        onPicked(images)
        retainedPickerDelegate = null
    }
}

private fun topViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val windows = mutableListOf<UIWindow>()
    app.connectedScenes.forEach { scene ->
        val windowScene = scene as? UIWindowScene ?: return@forEach
        windowScene.windows.forEach { window ->
            (window as? UIWindow)?.let { windows.add(it) }
        }
    }
    if (windows.isEmpty()) {
        app.keyWindow?.let { windows.add(it) }
        app.windows.forEach { window ->
            (window as? UIWindow)?.let { windows.add(it) }
        }
    }
    val window = windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller?.presentedViewController
    }
    return controller
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.scaledForUpload(): UIImage {
    val maxSide = 1600.0
    var newWidth = 0.0
    var newHeight = 0.0
    size.useContents {
        val longest = maxOf(width, height)
        if (longest <= maxSide || longest <= 0.0) return this@scaledForUpload
        val scale = maxSide / longest
        newWidth = width * scale
        newHeight = height * scale
    }
    if (newWidth <= 0.0 || newHeight <= 0.0) return this
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(newWidth, newHeight), false, 1.0)
    drawInRect(CGRectMake(0.0, 0.0, newWidth, newHeight))
    val scaled = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return scaled ?: this
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toJpegBytes(): ByteArray? {
    val data = UIImageJPEGRepresentation(this, 0.72) ?: return null
    return data.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val count = this.length
    val size = count.toInt()
    if (size <= 0) return ByteArray(0)
    val source = this.bytes ?: return ByteArray(0)
    val out = ByteArray(size)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, count.convert())
    }
    return out
}
