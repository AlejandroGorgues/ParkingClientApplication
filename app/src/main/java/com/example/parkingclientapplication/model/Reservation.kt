package com.example.parkingclientapplication.model

import android.os.Parcel
import android.os.Parcelable


class Reservation() : Parcelable {
    var id: String? = null

    var licensePlate: String? = null
    var model: String? = null
    var brand: String? = null
    var expensesActive: Float? = null
    var dateReservation: String? = null
    var nameParking: String? = null
    var state: String? = null
    var idDriver: String? = null
    var idParkingLot: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        licensePlate = parcel.readString()
        model = parcel.readString()
        brand = parcel.readString()
        expensesActive = parcel.readValue(Float::class.java.classLoader) as? Float
        dateReservation = parcel.readString()
        nameParking = parcel.readString()
        state = parcel.readString()
        idDriver = parcel.readString()
        idParkingLot = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(licensePlate)
        parcel.writeString(model)
        parcel.writeString(brand)
        parcel.writeValue(expensesActive)
        parcel.writeString(dateReservation)
        parcel.writeString(nameParking)
        parcel.writeString(state)
        parcel.writeString(idDriver)
        parcel.writeString(idParkingLot)
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