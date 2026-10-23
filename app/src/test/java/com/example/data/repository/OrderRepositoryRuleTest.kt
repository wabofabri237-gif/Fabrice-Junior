package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.domain.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class OrderRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createOrder_validPayload_createsDocumentAndReturnsOrder(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = OrderRepository(firestore)

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createOrder(
        customerName = "Alice Manga",
        sellerId = "seller_official",
        totalAmount = 25000.0,
        paymentMethod = "CASH_ON_DELIVERY",
        deliveryAddress = "Bonapriso, Douala",
        deliveryCity = "Douala",
        contactPhone = "+237699001122",
        itemsSummary = "1x Chaussures en cuir"
      )
    }
    assertTrue(createResult.isSuccess)
    val order = createResult.getOrThrow()
    assertTrue(order.id.isNotEmpty())
    assertEquals(uid, order.userId)
    assertTrue(order.deliveryOtp.isNotEmpty())
  }

  @Test
  fun observeUserOrders_authenticatedOwner_returnsCreatedOrder(): Unit = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = OrderRepository(firestore)

    val order = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createOrder(
        customerName = "Alice Manga",
        sellerId = "seller_official",
        totalAmount = 15000.0,
        paymentMethod = "BANK_TRANSFER",
        deliveryAddress = "Akwa, Douala",
        deliveryCity = "Douala",
        contactPhone = "+237677001122",
        itemsSummary = "1x Sac artisanal"
      ).getOrThrow()
    }

    // Direct Firestore fetch check ensuring query alignment and rule evaluation
    val querySnapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
      firestore.collection("orders").whereEqualTo("userId", uid).get().await()
    }
    val fetchedIds = querySnapshot.documents.map { it.id }
    assertTrue("Fetched IDs should contain created order", fetchedIds.contains(order.id))

    // Realtime flow check
    val emittedOrders = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserOrders(uid).first { list -> list.any { it.id == order.id } }
    }
    assertTrue(emittedOrders.any { it.id == order.id })
  }

  @Test
  fun unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    auth.signOut()

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("orders").get().await()
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@nboashop.cm"
    const val BOB_EMAIL = "bob@nboashop.cm"
    const val DEFAULT_TIMEOUT_MS = 10000L
    const val FLOW_TIMEOUT_MS = 10000L
  }
}
