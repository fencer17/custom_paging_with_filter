package co.library.custom_paging_with_filter

import android.content.Intent
import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import co.library.custom_paging_with_filter.ui.MainActivity
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Simply sanity test to ensure that activity launches without any issues and shows some data.
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {
    @Rule
    @JvmField
    val testRule = CountingTaskExecutorRule()

    @Test
    @Throws(InterruptedException::class, TimeoutException::class)
    fun showSomeResults() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = InstrumentationRegistry.getInstrumentation()
            .startActivitySync(intent)
        testRule.drainTasks(10, TimeUnit.SECONDS)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val recyclerView: RecyclerView = activity.findViewById(R.id.recyclerView)
        MatcherAssert.assertThat(recyclerView.adapter, CoreMatchers.notNullValue())
        waitForAdapterChange(recyclerView)
        MatcherAssert.assertThat(recyclerView.adapter?.itemCount ?: 0 > 0, CoreMatchers.`is`(true))
    }

    @Throws(InterruptedException::class)
    private fun waitForAdapterChange(recyclerView: RecyclerView) {
        val latch = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync {
                recyclerView.adapter?.registerAdapterDataObserver(
                    object : RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            latch.countDown()
                        }

                        override fun onChanged() {
                            latch.countDown()
                        }
                    })
            }
        if (recyclerView.adapter?.itemCount ?: 0 > 0) {
            return  //already loaded
        }
        MatcherAssert.assertThat(latch.await(10, TimeUnit.SECONDS), CoreMatchers.`is`(true))
    }

    @Test
    @Throws(InterruptedException::class, TimeoutException::class)
    fun insertItems() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent)

        onView(Matchers.allOf(withId(R.id.recyclerView), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        testRule.drainTasks(10, TimeUnit.SECONDS)

        val recyclerView: RecyclerView = activity.findViewById(R.id.recyclerView)

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        waitForAdapterChange(recyclerView)

        val itemCountBeforeInsert = recyclerView.adapter?.itemCount ?: 0

        onView(Matchers.allOf(withId(R.id.fab_insert), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.fab_insert)).perform(click())

        Thread.sleep(3000)

        MatcherAssert.assertThat(recyclerView.adapter?.itemCount ?: 0 > itemCountBeforeInsert, CoreMatchers.`is`(true))
    }

    @Test
    @Throws(InterruptedException::class, TimeoutException::class)
    fun deleteItems() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = InstrumentationRegistry.getInstrumentation().startActivitySync(intent)

        onView(Matchers.allOf(withId(R.id.recyclerView), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        testRule.drainTasks(10, TimeUnit.SECONDS)

        val recyclerView: RecyclerView = activity.findViewById(R.id.recyclerView)

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        waitForAdapterChange(recyclerView)

        val itemCountBeforeDelete = recyclerView.adapter?.itemCount ?: 0

        onView(Matchers.allOf(withId(R.id.fab_delete), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.fab_delete)).perform(click())

        Thread.sleep(3000)

        MatcherAssert.assertThat(recyclerView.adapter?.itemCount ?: 0 < itemCountBeforeDelete, CoreMatchers.`is`(true))
    }
}