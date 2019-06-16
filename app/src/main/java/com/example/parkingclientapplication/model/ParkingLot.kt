package com.example.parkingclientapplication.model

import android.os.Parcel
import android.os.Parcelable

class ParkingLot() : Parcelable {
    var id: String? = null
    var position: String? = null
    var stateLot: String? = null
    var idParking: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        position = parcel.readString()
        stateLot = parcel.readString()
        idParking = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(position)
        parcel.writeString(stateLot)
        parcel.writeString(idParking)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParkingLot> {
        override fun createFromParcel(parcel: Parcel): ParkingLot {
            return ParkingLot(parcel)
        }

        override fun newArray(size: Int): Array<ParkingLot?> {
            return arrayOfNulls(size)
        }
    }
}