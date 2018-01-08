package somerfield.howami.commsservice.jobs

import somerfield.time.TimeInterval
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

typealias Job = () -> Unit

class JobScheduler(private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()) {

    fun stop() {
        executor.shutdown()
    }

    fun schedule(job: Job, every: TimeInterval): JobControl {
        val scheduled = executor.scheduleAtFixedRate(job, 0, every.inMillis(), TimeUnit.MILLISECONDS)
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