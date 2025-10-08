import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

object FirebaseInit {

    /**
     * Enables offline persistence for Firestore.
     *
     * This sets up Firestore with a local cache that persists data across
     * app restarts and synchronizes with the cloud once connectivity is restored.
     *
     * Must be called before any Firestore operations (e.g., repository usage)
     * to ensure persistence is active for all instances.
     *
     * This should be called once in app startup (MainActivity onCreate).
     */
    fun enableOfflinePersistence() { //to be called in MainActivity in onCreate
        val firestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()

        firestore.firestoreSettings = settings
    }
}
