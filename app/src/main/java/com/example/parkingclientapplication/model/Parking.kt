package com.example.parkingclientapplication.model

import android.os.Parcel
import android.os.Parcelable

class Parking(): Parcelable {
    var id: String? = null
    var nameParking: String? =  null
    var direction: String? = null
    var latitude: Float? = null
    var longitude: Float? = null
    var price: Float? = null
    var stateParking: Boolean? = null
    var description: String? = null
    var maxOccupation: Int? = null
    var occupation: Int? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        nameParking = parcel.readString()
        direction = parcel.readString()
        latitude = parcel.readValue(Float::class.java.classLoader) as? Float
        longitude = parcel.readValue(Float::class.java.classLoader) as? Float
        price = parcel.readValue(Float::class.java.classLoader) as? Float
        stateParking = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        description = parcel.readString()
        maxOccupation = parcel.readInt()
        occupation = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nameParking)
        parcel.writeString(direction)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeValue(price)
        parcel.writeValue(stateParking)
        parcel.writeString(description)
        parcel.writeValue(maxOccupation)
        parcel.writeValue(occupation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Parking> {
        override fun createFromParcel(parcel: Parcel): Parking {
            return Parking(parcel)
        }

        override fun newArray(size: Int): Array<Parking?> {
            return arrayOfNulls(size)
        }
    }
}