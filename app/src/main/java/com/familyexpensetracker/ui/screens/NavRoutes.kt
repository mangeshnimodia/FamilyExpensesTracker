package com.familyexpensetracker.ui.screens

object NavRoutes {
    const val MAIN_LIST   = "main_list"
    const val ADD         = "add"
    const val EDIT        = "edit/{txnId}"
    const val COPY        = "copy/{txnId}"
    const val SUBCATEGORY = "subcategory/{category}"

    fun edit(txnId: String)      = "edit/$txnId"
    fun copy(txnId: String)      = "copy/$txnId"
    fun subcategory(cat: String) = "subcategory/$cat"
}
