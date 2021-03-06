package repository

import model.PostModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PostRepositoryInMemoryWithMutexImpl : PostRepository {
    private var nextId = 1L
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> =
        mutex.withLock {
            items.reversed()
        }

    override suspend fun getById(id: Long): PostModel? =
        mutex.withLock {
            items.find { it.id == id }
        }

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun save(item: PostModel): PostModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val dateCreated = LocalDateTime.now()
                    val dateId = ZoneId.of("Europe/Moscow")
                    val zonedDateTime = ZonedDateTime.of(dateCreated, dateId)
                    val copy = item.copy(id = nextId++, created = zonedDateTime)
                    items.add(copy)
                    copy
                }
                else -> {
                    // TODO:
                    items[index] = item
                    item
                }
            }
        }
    }

    override suspend fun likeById(id: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> {
                null
            }
            else -> {
                val item = items[index]
                val copy = item.copy(countLiked = item.countLiked + 1, likeByMe = true)
                items[index] = copy
                copy
            }
        }
    }

    override suspend fun commentById(id: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copy = item.copy(countComment = item.countComment+1)
                try {
                    items[index] = copy
                } catch (e: ArrayIndexOutOfBoundsException) {
                    println("size: ${items.size}")
                    println(index)
                }
                copy
            }
        }
    }

    override suspend fun shareById(id: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copy = item.copy(countShare = item.countShare+1)
                try {
                    items[index] = copy
                } catch (e: ArrayIndexOutOfBoundsException) {
                    println("size: ${items.size}")
                    println(index)
                }
                copy
            }
        }
    }
}



