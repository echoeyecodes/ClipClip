package com.echoeyecodes.clipclip.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.echoeyecodes.clipclip.fragments.dialogfragments.BaseDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.schedule

class AndroidUtilities {

    companion object {
        fun showSnackBar(view: View, message: String, action: (view: View) -> Unit = {}) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Dismiss") {
                action(view)
            }.show()
        }

        fun showSnackBar(
            view: View,
            message: String,
            actionText: String,
            action: (view: View) -> Unit = {}
        ) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction(actionText) {
                action(view)
            }.show()
        }

        fun getDrawable(context: Context, drawable: Int): Drawable? {
            return ResourcesCompat.getDrawable(context.resources, drawable, null)
        }

        fun dismissFragment(fragment: DialogFragment) {
            if (fragment.isAdded) {
                fragment.dismiss()
            }
        }

        fun showFragment(fragmentManager: FragmentManager, fragment: BaseDialogFragment) {
            if (!fragment.isAdded) {
                fragment.show(fragmentManager, fragment.getDialogTag())
            }
        }

        fun showToastMessage(context: Context, message: String) =
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        fun log(message: String) = Log.d("CARRR", message)
        fun runOnTimeout(time: Long, doWork: () -> Unit) = Timer().schedule(time) { doWork() }

        fun openShareIntent(link: String, activity: Activity) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, link)
                type = "text/plain"
            }
            activity.startActivity(Intent.createChooser(intent, "Share Post"))
        }
    }
}