package com.example.alladyn.model

data class CarpetModel(
    val length: Double = 0.0,
    val width: Double = 0.0,
    val metricArea: Double = 0.0,
    val type: String = "",
    val date: String = "",
    val pickUpPoint: String = "",
    val price: Double = 0.0,
    val photoBefore: ByteArray? = null,
    val photoMeasured: ByteArray? = null,
    val photoAfter: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CarpetModel

        if (length != other.length) return false
        if (width != other.width) return false
        if (metricArea != other.metricArea) return false
        if (type != other.type) return false
        if (date != other.date) return false
        if (pickUpPoint != other.pickUpPoint) return false
        if (price != other.price) return false
        if (photoBefore != null) {
            if (other.photoBefore == null) return false
            if (!photoBefore.contentEquals(other.photoBefore)) return false
        } else if (other.photoBefore != null) return false
        if (photoMeasured != null) {
            if (other.photoMeasured == null) return false
            if (!photoMeasured.contentEquals(other.photoMeasured)) return false
        } else if (other.photoMeasured != null) return false
        if (photoAfter != null) {
            if (other.photoAfter == null) return false
            if (!photoAfter.contentEquals(other.photoAfter)) return false
        } else if (other.photoAfter != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + metricArea.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + pickUpPoint.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + (photoBefore?.contentHashCode() ?: 0)
        result = 31 * result + (photoMeasured?.contentHashCode() ?: 0)
        result = 31 * result + (photoAfter?.contentHashCode() ?: 0)
        return result
    }
}
