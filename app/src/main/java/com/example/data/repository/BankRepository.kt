package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreProvider
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.domain.model.BankAccount
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class BankRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreProvider.get(context))

    val defaultCemacBanks = listOf(
        BankAccount(
            id = "afriland_cm",
            bankName = "Afriland First Bank (Cameroun)",
            accountName = "NBOASHOP SARL - RECOUVREMENT",
            iban = "CM21 1000 5000 1234 5678 9012 345",
            rib = "10005 00012 34567890123 45",
            swift = "AFRICMCX",
            active = true
        ),
        BankAccount(
            id = "uba_cm",
            bankName = "United Bank for Africa (UBA Cameroun)",
            accountName = "NBOASHOP SARL",
            iban = "CM21 1003 3000 9876 5432 1098 765",
            rib = "10033 00098 76543210987 65",
            swift = "UNAFCMCX",
            active = true
        ),
        BankAccount(
            id = "sg_cm",
            bankName = "Société Générale Cameroun (SGC)",
            accountName = "NBOASHOP SARL",
            iban = "CM21 1000 3000 5544 3322 1100 998",
            rib = "10003 00055 44332211009 98",
            swift = "SOGECMCX",
            active = true
        ),
        BankAccount(
            id = "bgfi_cm",
            bankName = "BGFIBank Cameroun",
            accountName = "NBOASHOP SARL",
            iban = "CM21 1003 5000 7788 9900 1122 334",
            rib = "10035 00077 88990011223 34",
            swift = "BGFICMCX",
            active = true
        )
    )

    fun observeBankAccounts(): Flow<List<BankAccount>> {
        return db.collection("bank_accounts")
            .snapshots()
            .map { snapshot ->
                val fromDb = snapshot.toObjects(BankAccount::class.java)
                if (fromDb.isNotEmpty()) fromDb else defaultCemacBanks
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "bank_accounts")
                emit(defaultCemacBanks)
            }
    }
}
