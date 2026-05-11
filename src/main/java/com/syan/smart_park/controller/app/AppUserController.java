package com.syan.smart_park.controller.app;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ParkUser;
import com.syan.smart_park.entity.ParkUserDTO;
import com.syan.smart_park.service.ParkUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app/user")
@RequiredArgsConstructor
public class AppUserController {

    private final ParkUserService parkUserService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        return null;
    }

    @GetMapping("/info")
    public R<ParkUserDTO> getUserInfo() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        ParkUserDTO user = parkUserService.getParkUserById(userId);
        if (user == null) {
            return R.error(ReturnCode.RC600);
        }
        return R.success(user);
    }

    @PostMapping("/bind-phone")
    public R<ParkUserDTO> bindPhone(@RequestParam String phone) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }

        ParkUserDTO existingUser = parkUserService.getByPhone(phone);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            return R.error(ReturnCode.RC603);
        }

        ParkUser parkUser = new ParkUser();
        parkUser.setId(userId);
        parkUser.setPhone(phone);

        ParkUserDTO updatedUser = parkUserService.updateParkUser(userId, parkUser);
        return R.success(updatedUser);
    }

    @PutMapping("/info")
    public R<ParkUserDTO> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }

        ParkUser parkUser = new ParkUser();
        parkUser.setId(userId);
        parkUser.setUsername(request.getUsername());
        parkUser.setPhone(request.getPhone());
        parkUser.setEmail(request.getEmail());
        parkUser.setAvatar(request.getAvatar());

        ParkUserDTO result = parkUserService.updateParkUser(userId, parkUser);
        return R.success(result);
    }

    @Data
    public static class UpdateUserInfoRequest {
        @Size(min = 1, max = 50, message = "用户名长度不能超过50个字符")
        private String username;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Size(max = 500, message = "头像URL长度不能超过500个字符")
        private String avatar;
    }
}
