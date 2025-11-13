package com.android.gatherly.utilstest

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/** This class is used to mock Firebase Authentication */
class MockitoUtils {
  lateinit var mockAuth: FirebaseAuth
  lateinit var mockUser: FirebaseUser

  init {
    initMockAuthAndUser()
  }

  /** To be called first. Defines the kind of classes mockAuth and mockUser mock */
  private fun initMockAuthAndUser() {
    mockAuth = mock(FirebaseAuth::class.java)
    mockUser = mock(FirebaseUser::class.java)
  }

  /**
   * Defines the currentUser returned when mocking. Also defines the authentication means of the
   * user
   *
   * @param currentUser the current user string chosen for testing
   * @param isAnon defines if a user is anonymously connected when mocking
   */
  fun chooseCurrentUser(currentUser: String, isAnon: Boolean = false) {
    `when`(mockAuth.currentUser).thenReturn(mockUser)
    `when`(mockUser.uid).thenReturn(currentUser)
    `when`(mockUser.isAnonymous).thenReturn(false)
  }

  /** For tests that want to test with an unauthenticated user */
  fun unauthenticatedCurrentUser() {
    `when`(mockAuth.currentUser).thenReturn(null)
  }
}
