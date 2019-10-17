/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studioexample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class FontFamilyAdapter(
  context: Context,
  private var listener: FontFamilyListener?,
  private val items: List<FontLoader.FontFamily>
) : ArrayAdapter<FontLoader.FontFamily>(context, R.layout.dropdown_menu_popup_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val row = convertView ?: inflater.inflate(R.layout.dropdown_menu_popup_item, parent, false)
        val item = items[position]

        with(row) {
            val textView = row as TextView
            textView.text = item.name
            textView.typeface = item.getTypeface(context.assets)
        }

        return row
    }

    override fun getItem(p0: Int): FontLoader.FontFamily {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    interface FontFamilyListener {
        fun onFontFamilySelect(fontFamily: FontLoader.FontFamily)
    }
}
