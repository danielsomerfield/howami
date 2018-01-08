package somerfield.howami.commsservice.jobs

import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.testing.IntegrationTests
import somerfield.time.seconds

@Category(IntegrationTests::class)
class JobSchedulerIntegrationTest {

    private var jobScheduler:JobScheduler? = null

    @Before
    fun init() {
        jobScheduler = JobScheduler()
    }

    @After
    fun cleanup() {
        jobScheduler?.stop()
    }

    @Test()
    fun jobRunsOnExpectedSchedule() {
        var runCount = 0

        jobScheduler!!.schedule(
                job = { runCount++ },
                every = 2.seconds()
        )

        Thread.sleep(7.seconds().inMillis())

        assertThat(runCount, `is`(4))

    }

    @Test()
    fun jobStops() {
        var runCount = 0

        val job = jobScheduler!!.schedule(
                job = { runCount++ },
                every = 2.seconds()
        )

        Thread.sleep(7.seconds().inMillis())

        assertThat(runCount, `is`(4))

        jobScheduler!!.stopJob(job)

        Thread.sleep(4.seconds().inMillis())

        assertThat(runCount, `is`(4))

    }

}


