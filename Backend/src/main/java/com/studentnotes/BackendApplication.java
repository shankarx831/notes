package com.studentnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Student Notes Platform Backend Application.
 * 
 * Features:
 * - Admin & Teacher Dashboard APIs
 * - JWT Authentication
 * - Role-based Authorization
 * - Audit Logging
 * - Caching for Dashboard Metrics
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
