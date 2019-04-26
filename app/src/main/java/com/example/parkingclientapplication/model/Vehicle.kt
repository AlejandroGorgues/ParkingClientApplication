package com.example.parkingclientapplication.model

import android.os.Parcel
import android.os.Parcelable

class Vehicle() : Parcelable {

    var id: String? = null

    var licensePlate: String? = null
    var model: String? = null
    var brand: String? = null
    var state: String? = null
    var type: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        licensePlate = parcel.readString()
        model = parcel.readString()
        brand = parcel.readString()
        state = parcel.readString()
        type = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(licensePlate)
        parcel.writeString(model)
        parcel.writeString(brand)
        parcel.writeString(state)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        override fun createFromParcel(parcel: Parcel): Vehicle {
            return Vehicle(parcel)
        }

        override fun newArray(size: Int): Array<Vehicle?> {
            return arrayOfNulls(size)
        }
    }
}