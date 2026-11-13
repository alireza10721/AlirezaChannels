package com.alireza10721.channels

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private val creator = "alireza10721@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showGate()
    }

    private fun base(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 24)
            gravity = Gravity.TOP
        }
        val heading = TextView(this).apply {
            text = title
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 22)
        }
        root.addView(heading)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun addText(text: String, size: Float = 16f) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = size
            setPadding(0, 8, 0, 12)
        })
    }

    private fun addButton(label: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        })
    }

    private fun showGate() {
        base("Alireza Channels")
        val saved = prefs.getString("pin", null)
        addText(if (saved == null) "برای شروع یک رمز عددی بسازید." else "رمز ورود را وارد کنید.")
        val pin = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "رمز عددی"
        }
        root.addView(pin)
        addButton(if (saved == null) "ثبت رمز و ورود" else "ورود") {
            val entered = pin.text.toString()
            if (entered.length < 4) {
                toast("رمز باید حداقل ۴ رقم باشد.")
            } else if (saved == null) {
                prefs.edit().putString("pin", entered).apply()
                showHome()
            } else if (entered == saved) showHome()
            else toast("رمز اشتباه است.")
        }
        addText("سازنده: $creator", 13f)
    }

    private fun showHome() {
        base("کانال‌های آنلاین")
        addText("این نسخه، پوسته اولیه برنامه است. برای نمایش کانال‌ها، لینک‌های معتبر و قانونی را اضافه کنید.")
        addButton("افزودن لینک کانال") {
            val input = EditText(this).apply { hint = "https://..." }
            AlertDialogBuilder.show(this, "لینک کانال", input) {
                val value = input.text.toString().trim()
                if (value.startsWith("https://")) {
                    val list = prefs.getStringSet("channels", emptySet())!!.toMutableSet()
                    list.add(value)
                    prefs.edit().putStringSet("channels", list).apply()
                    showHome()
                } else toast("فقط لینک HTTPS پذیرفته می‌شود.")
            }
        }
        addButton("مدیریت اتصال سرور") {
            base("تنظیمات سرور")
            addText("هنوز هیچ سروری تنظیم نشده است. برای تونل واقعی باید یک سرور VPN مجاز و فایل/لینک پیکربندی معتبر تهیه شود. این نسخه به‌تنهایی فیلترشکن نیست.")
            addText("پروتکل‌های قابل پشتیبانی در نسخه بعدی: WireGuard یا VLESS/VMess با هسته سازگار.")
            addButton("بازگشت") { showHome() }
        }
        val channels = prefs.getStringSet("channels", emptySet()) ?: emptySet()
        if (channels.isEmpty()) addText("هنوز کانالی اضافه نشده است.")
        channels.forEachIndexed { index, url ->
            addButton("باز کردن لینک ${index + 1}") {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                catch (_: Exception) { toast("باز کردن لینک ممکن نشد.") }
            }
        }
        addButton("تغییر رمز ورود") {
            val input = EditText(this).apply {
                hint = "رمز جدید (حداقل ۴ رقم)"
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }
            AlertDialogBuilder.show(this, "تغییر رمز", input) {
                val p = input.text.toString()
                if (p.length >= 4) { prefs.edit().putString("pin", p).apply(); toast("رمز تغییر کرد.") }
                else toast("رمز باید حداقل ۴ رقم باشد.")
            }
        }
        addText("سازنده: $creator", 13f)
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

private object AlertDialogBuilder {
    fun show(activity: Activity, title: String, view: View, onOk: () -> Unit) {
        android.app.AlertDialog.Builder(activity)
            .setTitle(title).setView(view)
            .setNegativeButton("انصراف", null)
            .setPositiveButton("ذخیره") { _, _ -> onOk() }
            .show()
    }
}
