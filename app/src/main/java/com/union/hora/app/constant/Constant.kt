package com.union.hora.app.constant


import com.union.hora.BuildConfig
import com.union.hora.HoraApp
import java.io.File

object  Constant {

    const val BASE_URL = BuildConfig.BASE_URL
    const val SCAN_URL = "https://api.psacard.com/"

    const val IMAGE_URL = BASE_URL + "cardserv/iss/download/"
    val IMAGE_TEMP = HoraApp.context.getExternalFilesDir("image-temp")?.path + File.separator
    val IMAGE_ORDER = HoraApp.context.getExternalFilesDir("image-order")?.path + File.separator
    val IMAGE_AVATAR = HoraApp.context.getExternalFilesDir("image-avatar")?.path + File.separator

    const val KEY_LOGIN_OUT = "login"
    const val KEY_LOGIN_USER = "loginUser"
    const val KEY_LOGIN_TYPE = "loginType"
    const val KEY_LOGIN_TOKEN = "loginToken"
    const val KEY_LOGIN_HISTORY = "loginHistory"
    const val KEY_HAS_NETWORK = "hasNetwork"
    const val KEY_BALANCE = "payoutBalance"
    const val KEY_EMPTY = "keepEmpty"
    const val KEY_VERSION = "version"

    /**
     * url key
     */

    const val STRIPE_SETUP = "setup"
    const val STRIPE_PAYMENT = "payment"
    const val CONTENT_ID_KEY = "id"

    const val UPLOAD_IMAGE_4 = 4
    const val UPLOAD_IMAGE_6 = 6
    const val UPLOAD_IMAGE_10 = 10

    const val NO_DATA = -100L

    object Extra {
        const val id = "id"
        const val USER_ID = "userId"
        const val DECK_ID = "deckId"
        const val PRODUCT_ID = "productId"
        const val USER_NAME = "userName"
        const val USER_AVATAR = "userAvatar"
        const val USER_BLOCK = "userBlock"
        const val USER_IS_PARTY = "isParty"
        const val USER_BIND_EMAIL = "bindEmail"

        const val CARD_NAME = "cardName"
        const val PAGE_LEVEL = "pageLevel"
        const val DECK_ITEM = "deckItem"
        const val DECK_BIND = "deckBind"
        const val PRODUCT_ITEM = "productItem"
        const val PUBLISH_SEARCH = "publishSearch"
        const val KEYWORD_SEARCH = "keywordSearch"

        const val LINK_PATH = "link_path"
        const val LINK_TITLE = "link_title"
        const val LINK_CONTENT = "link_content"

        const val SCAN_CERT_NUMBER = "certNumber"
        const val SCAN_BRAND = "brand"
        const val SCAN_SUBJECT = "subject"
        const val SCAN_PSA_CERT = "PSACert"
    }
}
