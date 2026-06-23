package com.monkeycode.aiscreen.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.monkeycode.aiscreen.core.data.local.AppDatabase
import com.monkeycode.aiscreen.core.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ConversationDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun should_insert_and_retrieve_conversation() = runBlocking {
        val entity = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = "Test Conversation",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        database.conversationDao().insert(entity)

        val retrieved = database.conversationDao().getById(entity.id)
        assertNotNull(retrieved)
        assertEquals("Test Conversation", retrieved?.title)
    }

    @Test
    fun should_list_conversations_in_desc_order() = runBlocking {
        val dao = database.conversationDao()
        val older = ConversationEntity("id1", "Older", 1000, 1000)
        val newer = ConversationEntity("id2", "Newer", 2000, 2000)

        dao.insert(older)
        dao.insert(newer)

        val list = dao.getRecent(10).first()
        assertEquals(2, list.size)
        assertEquals("Newer", list[0].title)
        assertEquals("Older", list[1].title)
    }

    @Test
    fun should_update_conversation_title() = runBlocking {
        val dao = database.conversationDao()
        val entity = ConversationEntity("id1", "Original", 1000, 1000)
        dao.insert(entity)

        dao.update("id1", "Updated", 2000)

        val updated = dao.getById("id1")
        assertEquals("Updated", updated?.title)
        assertEquals(2000, updated?.updatedAt)
    }

    @Test
    fun should_delete_conversation() = runBlocking {
        val dao = database.conversationDao()
        dao.insert(ConversationEntity("id1", "Test", 1000, 1000))

        dao.delete("id1")

        assertNull(dao.getById("id1"))
    }

    @Test
    fun should_count_conversations() = runBlocking {
        val dao = database.conversationDao()
        repeat(3) {
            dao.insert(ConversationEntity("id$it", "Conv $it", 1000, 1000))
        }

        assertEquals(3, dao.count())
    }

    @Test
    fun should_delete_oldest_entries_for_cache_limit() = runBlocking {
        val dao = database.conversationDao()
        repeat(5) {
            dao.insert(ConversationEntity("id$it", "Conv $it", it.toLong() * 1000, it.toLong() * 1000))
        }

        assertEquals(5, dao.count())

        dao.deleteOldest(2)

        assertEquals(3, dao.count())
    }
}
