package com.app.scheduler.sheduler.controller;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.scheduler.sheduler.payload.EmailRequest;
import com.app.scheduler.sheduler.payload.EmailResponse;
import com.app.scheduler.sheduler.quartz.job.EmailJob;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class EmailSchedulerController {

	@Autowired
	private Scheduler scheduler;

	@GetMapping("/get")
	public ResponseEntity<String> testApi() {
		return ResponseEntity.ok("test api");
	}

	@PostMapping(value = "/schedule/email",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
		log.info("inside api  "+emailRequest.getEmail());
		try {
			ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());
			if (dateTime.isBefore(ZonedDateTime.now())) {
				EmailResponse emailResponse = new EmailResponse(false, "Datetime must be after current time");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emailResponse);
			}
			JobDetail jobDetail = buildJObDetail(emailRequest);
			Trigger trigger = buildTrigger(jobDetail, dateTime);
			scheduler.scheduleJob(jobDetail, trigger);
			EmailResponse emailResponse = new EmailResponse(true, jobDetail.getKey().getName(),
					jobDetail.getKey().getGroup(), "Email Scheduled Successfully");
			return ResponseEntity.ok(emailResponse);
		} catch (SchedulerException e) {
			log.error("Error while scheduling email :", e);
			EmailResponse emailResponse = new EmailResponse(false,
					"Error while scheduling email, Please try again later");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
		}
	}

	private JobDetail buildJObDetail(EmailRequest scheduleEmailRequest) {
		JobDataMap jobDataMap = new JobDataMap();

		jobDataMap.put("email", scheduleEmailRequest.getEmail());
		jobDataMap.put("subject", scheduleEmailRequest.getSubject());
		jobDataMap.put("body", scheduleEmailRequest.getBody());

		return JobBuilder.newJob(EmailJob.class).withIdentity(UUID.randomUUID().toString(), "email-job")
				.withDescription("Sent Email Job").usingJobData(jobDataMap).storeDurably().build();
	}

	private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {

		return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(jobDetail.getKey().getName(), "email-trigger")
				.withDescription("Send Email Trigger").startAt(Date.from(startAt.toInstant()))
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()).build();
	}
}
