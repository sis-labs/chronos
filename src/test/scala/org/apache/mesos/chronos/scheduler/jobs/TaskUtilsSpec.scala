package org.apache.mesos.chronos.scheduler.jobs

import org.joda.time._
import org.specs2.mock._
import org.specs2.mutable._

class TaskUtilsSpec extends SpecificationWithJUnit with Mockito {

  "TaskUtils" should {
    "Get taskId" in {
      val schedule = "R/2012-01-01T00:00:01.000Z/P1M"
      val arguments = "-a 1 -b 2"
      val cmdArgs = "-c 1 -d 2"
      val job1 = new ScheduleBasedJob(schedule, "sample-name", "sample-command", arguments = List(arguments))
      val job2 = new ScheduleBasedJob(schedule, "sample-name", "sample-command")
      val job3 = new ScheduleBasedJob(schedule, "sample-name", "sample-command", arguments = List(arguments))
      val ts = 1420843781398L
      val due = new DateTime(ts)

      val taskIdOne = TaskUtils.getTaskId(job1, due, 0)
      val taskIdTwo = TaskUtils.getTaskId(job2, due, 0)
      val taskIdThree = TaskUtils.getTaskId(job3, due, 0, Option(cmdArgs))
      val taskIdFour = TaskUtils.getTaskId(job2, due, 0, Option(cmdArgs))

      taskIdOne must_== "ct:1420843781398:0:sample-name:" + arguments
      taskIdTwo must_== "ct:1420843781398:0:sample-name:"
      taskIdThree must_== "ct:1420843781398:0:sample-name:" + cmdArgs    // test override
      taskIdFour must_== "ct:1420843781398:0:sample-name:" + cmdArgs    // test adding args
    }

    "Get job arguments for taskId" in {
      val arguments = "-a 1 -b 2"
      var taskId = "ct:1420843781398:0:test:" + arguments
      val jobArguments = TaskUtils.getJobArgumentsForTaskId(taskId)

      jobArguments must_== arguments
    }

    "Disable command injection" in {
      val schedule = "R/2012-01-01T00:00:01.000Z/P1M"
      val cmdArgs = "-c 1 ; ./evil.sh"
      val expectedArgs = "-c 1  ./evil.sh"
      val job1 = new ScheduleBasedJob(schedule, "sample-name", "sample-command")
      val ts = 1420843781398L
      val due = new DateTime(ts)

      val taskIdOne = TaskUtils.getTaskId(job1, due, 0, Option(cmdArgs))

      taskIdOne must_== "ct:1420843781398:0:sample-name:" + expectedArgs
    }

    "Parse taskId" in {
      val arguments = "-a 1 -b 2"
      val arguments2 = "-a 1:2 --B test"

      val taskIdOne = "ct:1420843781398:0:test:" + arguments
      val (jobName, jobDue, attempt, jobArguments) = TaskUtils.parseTaskId(taskIdOne)

      jobName must_== "test"
      jobDue must_== 1420843781398L
      attempt must_== 0
      jobArguments must_== arguments

      val taskIdTwo = "ct:1420843781398:0:test:" + arguments2
      val (_, _, _, jobArguments2) = TaskUtils.parseTaskId(taskIdTwo)

      jobArguments2 must_== arguments2

      val taskIdThree = "ct:1420843781398:0:test"
      val (jobName3, _, _, jobArguments3) = TaskUtils.parseTaskId(taskIdThree)

      jobName3 must_== "test"
      jobArguments3 must_== ""
    }
  }
}

