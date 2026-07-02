package com.familyexpensetracker.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TransactionRowMapperTest {

    private lateinit var mapper: TransactionRowMapper

    @Before
    fun setUp() {
        mapper = TransactionRowMapper()
    }

    @Test
    fun `mapRow maps all 9 fields correctly`() {
        val row = listOf("txn-1", "2026/06/15", "350.5", "Transport", "Fuel", "Cash", "petrol fill", "Wallet", "TRF-1")
        val t = mapper.mapRow(row)
        assertEquals("txn-1",       t.txnId)
        assertEquals("2026/06/15",  t.date)
        assertEquals(350.5,         t.amount, 0.0)
        assertEquals("Transport",   t.category)
        assertEquals("Fuel",        t.subcategory)
        assertEquals("Cash",        t.paymentMethod)
        assertEquals("petrol fill", t.description)
        assertEquals("Wallet",      t.account)
        assertEquals("TRF-1",       t.transferId)
    }

    @Test
    fun `mapRow defaults missing columns to empty string`() {
        val row = listOf<Any>()
        val t = mapper.mapRow(row)
        assertEquals("", t.txnId)
        assertEquals("", t.date)
        assertEquals(0.0, t.amount, 0.0)
        assertEquals("", t.category)
        assertEquals("", t.subcategory)
        assertEquals("", t.paymentMethod)
        assertEquals("", t.description)
        assertEquals("", t.account)
        assertNull(t.transferId)
    }

    @Test
    fun `mapRow defaults amount to 0 when not parseable`() {
        val row = listOf("1", "2026/06/01", "not-a-number", "Food", "Groceries", "UPI", "desc", "Savings", "")
        val t = mapper.mapRow(row)
        assertEquals(0.0, t.amount, 0.0)
    }

    @Test
    fun `mapRow handles absent transferId column as null`() {
        val row = listOf("1", "2026/06/01", "100.0", "Food", "Groceries", "UPI", "desc", "Savings")
        val t = mapper.mapRow(row)
        assertNull(t.transferId)
    }

    @Test
    fun `mapRow handles negative amount`() {
        val row = listOf("1", "2026/06/01", "-500.0", "Food", "Groceries", "UPI", "desc", "Savings", "")
        val t = mapper.mapRow(row)
        assertEquals(-500.0, t.amount, 0.0)
    }

    @Test
    fun `mapRow handles partial row with only txnId`() {
        val row = listOf<Any>("txn-99")
        val t = mapper.mapRow(row)
        assertEquals("txn-99", t.txnId)
        assertEquals("", t.date)
        assertEquals(0.0, t.amount, 0.0)
    }
}
