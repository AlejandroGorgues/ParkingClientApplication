package com.example.parkingclientapplication

/*
*Class to obtain the instance of the azure client to being used as a companion object
* Obtained from https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e which based on
* my knowledge is a bit complicated to understand, but in order to create a singleton, it works
*
* It implements Contravariance tpye (with out T) and a Convariance type (with in A) in order to instantiate the Singleton
 */
open class SingletonHolder<out T, in A>(creator: (A) -> T) {

    //It creates a creator variable that it supose to return an AzureClient named creator
    private var creator: ((A) -> T)? = creator
    //Volatile means all changes from one thread inmediately made visible to other threads
    @Volatile private var instance: T? = null


    //Based on the argument of type A which in this case is the context, it returns the Client of Azure
    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        /*
        * Implements the double-checked locking algorithm
        * Means to check if we need to create the instance that would cause to lock it (else)
        * or whe can obtain the instance without locking it (if)
         */
        return synchronized(this) {
            //Return the instance without locking it
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                //Create the AzureClient object
                val created = creator!!(arg)
                instance = created
                creator = null
                //Return the AzureClient
                created
            }
        }
    }
}