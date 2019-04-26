package com.example.parkingclientapplication.model

import android.os.Parcel
import android.os.Parcelable
import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset
import java.sql.Time
import java.sql.Timestamp
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class Reservation() : Parcelable {
    var id: String? = null

    var licensePlate: String? = null
    var model: String? = null
    var brand: String? = null
    var expensesActive: Float? = null
    var dateReservation: String? = null
    var timeActive: String? = null
    var state: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        licensePlate = parcel.readString()
        model = parcel.readString()
        brand = parcel.readString()
        expensesActive = parcel.readValue(Float::class.java.classLoader) as? Float
        dateReservation = parcel.readString()
        timeActive = parcel.readString()
        state = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(licensePlate)
        parcel.writeString(model)
        parcel.writeString(brand)
        parcel.writeValue(expensesActive)
        parcel.writeString(dateReservation)
        parcel.writeString(timeActive)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reservation> {
        override fun createFromParcel(parcel: Parcel): Reservation {
            return Reservation(parcel)
        }

        override fun newArray(size: Int): Array<Reservation?> {
            return arrayOfNulls(size)
        }
    }
}