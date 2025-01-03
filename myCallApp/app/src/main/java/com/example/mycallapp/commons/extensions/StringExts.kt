package com.example.mycallapp.commons.extensions

import android.net.Uri
import com.example.mycallapp.commons.COUNTRY_PREFIX
import com.example.mycallapp.commons.TEL_PREFIX

/**
 * Removes 'tel:' prefix from phone number string.
 */
fun String.removeTelPrefix() = this.replace(TEL_PREFIX, "")

fun String.removeCountryPrefix() = this.replace(COUNTRY_PREFIX, "")

/**
 * Phone call numbers can contain prefix of country like '+385' and '+' sign will be interpreted
 * like '%2B', so this must be decoded.
 */
fun String.parseCountryCode(): String = Uri.decode(this)