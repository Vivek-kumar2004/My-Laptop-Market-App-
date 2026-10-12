package com.mylaptopmarket.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

private var retainedExcelDelegate: NSObject? = null

@Composable
actual fun ExcelPicker(
    onFileSelected: (fileName: String, bytes: ByteArray) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val latest = rememberUpdatedState(onFileSelected)
    content {
        val delegate = ExcelDocumentDelegate { name, bytes ->
            latest.value(name, bytes)
        }
        retainedExcelDelegate = delegate
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf(
                "org.openxmlformats.spreadsheetml.sheet",
                "com.microsoft.excel.xls"
            ),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        picker.delegate = delegate
        picker.allowsMultipleSelection = false
        excelTopViewController()?.presentViewController(picker, true, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ExcelDocumentDelegate(
    private val onPicked: (String, ByteArray) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        controller.dismissViewControllerAnimated(true, null)
        val name = url?.lastPathComponent ?: "stock.xlsx"
        val bytes = url?.let { NSData.dataWithContentsOfURL(it)?.toByteArray() }
        dispatch_async(dispatch_get_main_queue()) {
            if (bytes != null) onPicked(name, bytes)
            retainedExcelDelegate = null
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        controller.dismissViewControllerAnimated(true, null)
        retainedExcelDelegate = null
    }
}

private fun excelTopViewController(): platform.UIKit.UIViewController? {
    val app = platform.UIKit.UIApplication.sharedApplication
    val windows = mutableListOf<platform.UIKit.UIWindow>()
    app.connectedScenes.forEach { scene ->
        val windowScene = scene as? platform.UIKit.UIWindowScene ?: return@forEach
        windowScene.windows.forEach { window ->
            (window as? platform.UIKit.UIWindow)?.let { windows.add(it) }
        }
    }
    if (windows.isEmpty()) {
        app.keyWindow?.let { windows.add(it) }
    }
    val window = windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)
    val source = bytes ?: return ByteArray(0)
    val out = ByteArray(size)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), source, length.convert())
    }
    return out
}
