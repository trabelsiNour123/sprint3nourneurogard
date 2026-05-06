package com.neuroguard.productorderservice.client;

import com.neuroguard.productorderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/internal/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}
