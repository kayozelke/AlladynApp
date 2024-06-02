package com.example.alladyn.model

data class CarpetModel(
//    Basic metric data
    val length: Double = 0.0,
    val width: Double = 0.0,
    val metricArea: Double = 0.0,
//    individual client or partner company
    val ifPrivateClient: Boolean = false,
    val ifCompanyPartner: Boolean = false,
//    Individual client data
    val ownerSurname: String = "",
    val ownerPhoneNumber: String = "",
//    Partner company data
    val pickUpPoint: String = "",
    val carpetNumber: String = "",
//    Other necessary data
    val date: String = "",
    val price: Double = 0.0,
//    Carpet photos
    val photoBefore: String? = null,
    val photoMeasured: String? = null,
    val photoAfter: String? = null
)

