package com.aghajari.zoomhelper.recycler

import android.content.Context
import com.aghajari.zoomhelper.R


object FakeData {
    val image = "https://picsum.photos/520/320?id="
    val names = listOf<String>(
        "Matt LeBlanc", "Matthew Perry", "David Schwimmer",
        "Courteney Cox", "Jennifer Aniston", "Lisa Kudrow"
    )
    val text =
        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu."

    fun fakeData(context: Context): List<Model> {
        val height = context.resources.displayMetrics.widthPixels * 320 / 520;
        val list = arrayListOf<Model>()
        for (j in 0..3) {
            for (i in 0 until names.size) {
                list.add(
                    Model(
                        findAvatar(i),
                        image + (i + (j * names.size)).toString(),
                        names[i],
                        text,
                        height
                    )
                )
            }
        }
        return list
    }

    private fun findAvatar(i: Int): Int {
        return when (i) {
            0 -> R.drawable.matt_leblanc
            1 -> R.drawable.matthew_perry
            2 -> R.drawable.david_schwimmer
            3 -> R.drawable.courteney_cox
            4 -> R.drawable.jennifer_aniston
            5 -> R.drawable.lisa_kudrow
            else -> R.drawable.matt_leblanc
        }
    }
}