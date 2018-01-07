package somerfield.howami.commsservice.jobs

import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import somerfield.testing.IntegrationTests
import java.time.Instant

@Category(IntegrationTests::class)
class JobSchedulerIntegrationTest {

    private val jobScheduler = JobScheduler()

    @Before
    fun init() {
        jobScheduler.start()
    }

    @After
    fun cleanup() {
        jobScheduler.stop()
    }

    @Test()
    fun jobRunsOnExpectedSchedule() {
        var runCount = 0

        val jobId = jobScheduler.schedule(
                job = { runCount++ },
                every = 2.seconds()
        )

        Thread.sleep(7.seconds().toMillis())

        assertThat(runCount, `is`(4))

        jobScheduler.stopJob(jobId)

        Thread.sleep(3.seconds().toMillis())
        assertThat(runCount, `is`(4))

    }
}


