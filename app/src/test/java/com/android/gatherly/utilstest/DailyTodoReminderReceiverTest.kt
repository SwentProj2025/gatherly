package com.android.gatherly.utilstest

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.gatherly.model.notification.NotificationsLocalRepository
import com.android.gatherly.model.notification.NotificationsRepository
import com.android.gatherly.model.todo.ToDo
import com.android.gatherly.model.todo.ToDoStatus
import com.android.gatherly.model.todo.ToDosLocalRepository
import com.android.gatherly.model.todo.ToDosRepository
import com.android.gatherly.utils.DailyTodoAlarmScheduler
import com.android.gatherly.utils.DailyTodoReminderReceiver
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

private const val DELAY = 500L

/** Instrumented tests for [DailyTodoReminderReceiver], which will execute on an Android device. */
@RunWith(AndroidJUnit4::class)
class DailyTodoReminderReceiverInstrumentedTest {

  private lateinit var context: Context
  private lateinit var todoRepository: ToDosRepository
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var fakeAlarmScheduler: FakeDailyTodoAlarmScheduler

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    todoRepository = ToDosLocalRepository()
    notificationsRepository = NotificationsLocalRepository()
    fakeAlarmScheduler = FakeDailyTodoAlarmScheduler(context)

    DailyTodoReminderReceiver.Companion.setTestProviders(
        todoProvider = { todoRepository },
        notifProvider = { notificationsRepository },
        alarmProvider = { fakeAlarmScheduler })
  }

  @After
  fun tearDown() {
    DailyTodoReminderReceiver.Companion.resetProviders()
  }

  /** Test that the receiver generates notifications for due todos and reschedules the alarm. */
  @Test
  fun receiver_generates_notifications_and_reschedules_alarm() = runBlocking {
    // Arrange
    val userId = "testUser"

    todoRepository.addTodo(
        ToDo(
            uid = "todo1",
            name = "Test Todo",
            description = "desc",
            dueDate = Timestamp.now(),
            dueTime = null,
            location = null,
            status = ToDoStatus.ONGOING,
            ownerId = userId))

    val intent =
        Intent(context, DailyTodoReminderReceiver::class.java).apply {
          putExtra(DailyTodoAlarmScheduler.Companion.EXTRA_USER_ID, userId)
        }

    val receiver = DailyTodoReminderReceiver()

    // Act
    receiver.onReceive(context, intent)
    delay(DELAY)

    // Assertions
    Assert.assertTrue(
        "Expected a notification to be created",
        notificationsRepository.getUserNotifications(userId).any { it.relatedEntityId == "todo1" })

    Assert.assertTrue("Expected alarm rescheduling", fakeAlarmScheduler.wasCalled)
  }

  /** Test that the receiver does nothing when no user ID is provided in the intent. */
  @Test
  fun receiver_withoutUserId_doesNothing() = runBlocking {
    val userId = "testUser"
    val intent = Intent(context, DailyTodoReminderReceiver::class.java)
    val receiver = DailyTodoReminderReceiver()

    receiver.onReceive(context, intent)
    delay(DELAY)

    Assert.assertTrue(
        "No notifications should be created",
        notificationsRepository.getUserNotifications(userId).isEmpty())

    Assert.assertFalse("Alarm should NOT be rescheduled", fakeAlarmScheduler.wasCalled)
  }

  /** Test that the receiver reschedules the alarm even when there are no todos. */
  @Test
  fun receiver_withNoTodos_reschedulesAlarm_withoutCreatingNotifications() = runBlocking {
    val userId = "user1"

    val intent =
        Intent(context, DailyTodoReminderReceiver::class.java).apply {
          putExtra(DailyTodoAlarmScheduler.EXTRA_USER_ID, userId)
        }

    val receiver = DailyTodoReminderReceiver()
    receiver.onReceive(context, intent)
    delay(DELAY)

    Assert.assertTrue(
        "No notifications should be generated",
        notificationsRepository.getUserNotifications(userId).isEmpty())

    Assert.assertTrue("Alarm should be rescheduled even if empty", fakeAlarmScheduler.wasCalled)
  }

  /** Test that the receiver ignores todos owned by another user. */
  @Test
  fun receiver_ignoresTodosOwnedByAnotherUser() = runBlocking {
    val todoRepositoryUser = ToDosLocalRepository("user1")
    todoRepositoryUser.addTodo(
        ToDo(
            uid = "todoX",
            name = "Wrong User Todo",
            description = "desc",
            dueDate = Timestamp.now(),
            dueTime = null,
            location = null,
            status = ToDoStatus.ONGOING,
            ownerId = "differentUser"))

    val intent =
        Intent(context, DailyTodoReminderReceiver::class.java).apply {
          putExtra(DailyTodoAlarmScheduler.EXTRA_USER_ID, "user1")
        }

    val receiver = DailyTodoReminderReceiver()
    receiver.onReceive(context, intent)
    delay(DELAY)

    Assert.assertTrue(
        "Receiver must not notify about someone else's todo",
        notificationsRepository.getUserNotifications("user1").isEmpty())

    Assert.assertTrue("Alarm should still be rescheduled", fakeAlarmScheduler.wasCalled)
  }

  /** Test that the receiver generates notifications for multiple todos. */
  @Test
  fun receiver_generatesNotificationsForMultipleTodos() = runBlocking {
    val userId = "john"

    todoRepository.addTodo(
        ToDo("t1", "A", "d", Timestamp.now(), null, null, ToDoStatus.ONGOING, userId))
    todoRepository.addTodo(
        ToDo("t2", "B", "d", Timestamp.now(), null, null, ToDoStatus.ONGOING, userId))

    val intent =
        Intent(context, DailyTodoReminderReceiver::class.java).apply {
          putExtra(DailyTodoAlarmScheduler.EXTRA_USER_ID, userId)
        }

    val receiver = DailyTodoReminderReceiver()
    receiver.onReceive(context, intent)
    delay(DELAY)

    val notifList = notificationsRepository.getUserNotifications(userId)

    Assert.assertEquals("Expected 2 notifications created", 2, notifList.size)

    Assert.assertTrue(fakeAlarmScheduler.wasCalled)
  }
}

/** A fake implementation of [DailyTodoAlarmScheduler] for testing purposes. */
class FakeDailyTodoAlarmScheduler(context: Context) : DailyTodoAlarmScheduler(context) {
  var wasCalled = false

  override fun scheduleNextTodoCheck(userId: String) {
    wasCalled = true
  }
}
