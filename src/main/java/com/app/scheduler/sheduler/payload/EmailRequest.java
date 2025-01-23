package com.app.scheduler.sheduler.payload;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {
	
	@Email
	@NotEmpty
	private String email;
	@NotEmpty
	private String subject;
	@NotEmpty
	private String body;
	@NotNull
	private LocalDateTime dateTime;
	@NotNull
	private ZoneId timeZone;

} 
