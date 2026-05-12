package com.syan.smart_park.controller.app;

import com.syan.smart_park.common.PageResult;
import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.VehicleDTO;
import com.syan.smart_park.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.Data;
import java.util.List;

@RestController
@RequestMapping("/api/app/vehicle")
@RequiredArgsConstructor
public class AppVehicleController {

    private final VehicleService vehicleService;

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

    @GetMapping("/my")
    public R<List<VehicleDTO>> getMyVehicles() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }
        PageResult<VehicleDTO> pageResult = vehicleService.listVehicles(userId, null, null, null, null, 1, Integer.MAX_VALUE);
        return R.success(pageResult.getRecords());
    }

    @PostMapping
    public R<VehicleDTO> addVehicle(@RequestBody CreateVehicleRequest request) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }

        VehicleDTO dto = new VehicleDTO();
        dto.setUserId(userId);
        dto.setPlateNumber(request.getPlateNumber());
        dto.setVehicleType(request.getVehicleType());
        dto.setBrand(request.getBrand());
        dto.setColor(request.getColor());
        dto.setStatus(1);

        VehicleDTO result = vehicleService.createVehicle(dto);
        if (result == null) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(result);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> deleteVehicle(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.unauthorized();
        }

        VehicleDTO vehicle = vehicleService.getVehicleById(id);
        if (vehicle == null) {
            return R.error(ReturnCode.RC500);
        }
        if (!vehicle.getUserId().equals(userId)) {
            return R.error(ReturnCode.RC403);
        }

        boolean success = vehicleService.deleteVehicle(id);
        return R.success(success);
    }

    @Data
    public static class CreateVehicleRequest {
        private String plateNumber;
        private Integer vehicleType;
        private String brand;
        private String color;
    }
}
