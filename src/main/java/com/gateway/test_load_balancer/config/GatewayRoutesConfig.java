package com.gateway.test_load_balancer.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Project: test-load-balancer
 * @Package: com.gateway.test_load_balancer.config  *
 * @Author: ChuVanNam
 * @Date: 10/17/2025
 * @Time: 10:39 AM
 */


@Configuration
public class GatewayRoutesConfig {

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("echo_route", r -> r.path("/**")
						.filters(f -> f.addResponseHeader("X-Handled-By",
								System.getenv("HOSTNAME") == null ? "local-instance" : System.getenv("HOSTNAME")))
						.uri("http://example.org")) // bạn có thể trỏ tới backend khác
				.build();
	}
}
