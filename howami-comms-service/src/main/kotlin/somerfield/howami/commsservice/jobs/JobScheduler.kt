package somerfield.howami.commsservice.jobs

import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class JobScheduler {

    private var maybeExecutor = Optional.of(createExecutor())

    fun start() {
        maybeExecutor.ifPresent {
            if (it.isShutdown) {
                maybeExecutor = Optional.of(createExecutor())
            }
        }
    }

    private fun createExecutor() = Executors.newSingleThreadScheduledExecutor()

    fun stop() {
        maybeExecutor.ifPresent { it.shutdown() }
    }

    fun schedule(job: () -> Unit, every: TimeInterval): JobControl {
        val scheduled = maybeExecutor.orElseThrow { RuntimeException("") }
                .scheduleAtFixedRate(job, 0, every.toMillis(), TimeUnit.MILLISECONDS)

        return JobControl(scheduled)
    }

    fun stopJob(jobId: JobControl) {
        jobId.stop()
    }

}

data class JobControl(private val future: Future<*>) {
    fun stop(mayInterruptIfRunning: Boolean = false) {
        future.cancel(mayInterruptIfRunning)
    }
}

data class TimeInterval(private val millis: Long) {
    fun toMillis() = millis;
}

fun Number.seconds(): TimeInterval {
    return TimeInterval(this.toLong() * 1000)
}