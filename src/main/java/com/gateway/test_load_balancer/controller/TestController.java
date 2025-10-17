package com.gateway.test_load_balancer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Project: test-load-balancer
 * @Package: com.gateway.test_load_balancer.controller  *
 * @Author: ChuVanNam
 * @Date: 10/17/2025
 * @Time: 10:42 AM
 */

@RestController
public class TestController {
	@GetMapping("/whoami")
	public String whoami() {
		return "✅ Gateway handled by pod: " +
				(System.getenv("HOSTNAME") != null ? System.getenv("HOSTNAME") : "local-instance");
	}
}