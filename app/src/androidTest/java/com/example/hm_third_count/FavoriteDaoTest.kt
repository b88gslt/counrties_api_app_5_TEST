package com.example.hm_third_count

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.local.FavoriteDao
import com.example.hm_third_count.data.local.FavoriteEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.favoriteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // Интеграционный тест 1: insert → read → данные корректно читаются повторно
    @Test
    fun insertAndReadFavorite() = runTest {
        dao.insert(FavoriteEntity("DEU"))

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("DEU", all.first().countryCode)
        assertTrue(dao.isFavorite("DEU"))
    }

    // Интеграционный тест 2 (нетривиальный): повторное добавление не создаёт дубль
    @Test
    fun duplicateInsertDoesNotCreateDuplicate() = runTest {
        dao.insert(FavoriteEntity("DEU"))
        dao.insert(FavoriteEntity("DEU")) // второй раз — должен игнорироваться

        val all = dao.getAll()
        assertEquals(1, all.size)
    }

    // Интеграционный тест 3: delete удаляет запись, isFavorite возвращает false
    @Test
    fun deleteRemovesFavorite() = runTest {
        dao.insert(FavoriteEntity("FRA"))
        dao.delete(FavoriteEntity("FRA"))

        assertFalse(dao.isFavorite("FRA"))
        assertTrue(dao.getAll().isEmpty())
    }
}
