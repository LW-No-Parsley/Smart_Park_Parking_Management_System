package com.syan.smart_park.controller.app;

import com.syan.smart_park.common.R;
import com.syan.smart_park.common.exception.ReturnCode;
import com.syan.smart_park.entity.ParkAreaDTO;
import com.syan.smart_park.entity.ParkAreaOccupancyStats;
import com.syan.smart_park.entity.ParkingSpaceDTO;
import com.syan.smart_park.entity.ParkingZoneDTO;
import com.syan.smart_park.service.ParkAreaService;
import com.syan.smart_park.service.ParkingSpaceService;
import com.syan.smart_park.service.ParkingZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppParkController {

    private final ParkAreaService parkAreaService;
    private final ParkingZoneService parkingZoneService;
    private final ParkingSpaceService parkingSpaceService;

    @GetMapping("/park-area/list")
    public R<List<ParkAreaDTO>> getParkAreaList() {
        List<ParkAreaDTO> parkAreas = parkAreaService.getParkAreasByStatus(1);
        return R.success(parkAreas);
    }

    @GetMapping("/park-area/{id}")
    public R<ParkAreaDTO> getParkAreaById(@PathVariable Long id) {
        ParkAreaDTO parkAreaDTO = parkAreaService.getParkAreaById(id);
        if (parkAreaDTO == null) {
            return R.error(ReturnCode.RC601);
        }
        return R.success(parkAreaDTO);
    }

    @GetMapping("/park-area/{id}/occupancy-stats")
    public R<ParkAreaOccupancyStats> getOccupancyStats(@PathVariable Long id) {
        ParkAreaOccupancyStats stats = parkAreaService.getParkAreaOccupancyStats(id);
        if (stats == null) {
            return R.error(ReturnCode.RC601);
        }
        return R.success(stats);
    }

    @GetMapping("/park-area/{parkAreaId}/zones")
    public R<List<ParkingZoneDTO>> getZonesByParkArea(@PathVariable Long parkAreaId) {
        List<ParkingZoneDTO> zones = parkingZoneService.getParkingZonesByParkAreaId(parkAreaId);
        return R.success(zones);
    }

    @GetMapping("/parking-space/available")
    public R<List<ParkingSpaceDTO>> getAvailableSpaces(
            @RequestParam(required = false) Long parkAreaId,
            @RequestParam(required = false) String time) {
        if (parkAreaId != null) {
            List<ParkingSpaceDTO> spaces = parkingSpaceService.getParkingSpacesByParkAreaIdWithOccupiedStatus(parkAreaId);
            spaces.removeIf(s -> s.getCurrentOccupiedStatus() != null && s.getCurrentOccupiedStatus() == 1);
            return R.success(spaces);
        }
        List<ParkingSpaceDTO> spaces = parkingSpaceService.getAvailableParkingSpaces(time);
        return R.success(spaces);
    }

    @GetMapping("/parking-space/{id}")
    public R<ParkingSpaceDTO> getParkingSpaceById(@PathVariable Long id) {
        ParkingSpaceDTO space = parkingSpaceService.getParkingSpaceWithOccupiedStatus(id);
        if (space == null) {
            return R.error(ReturnCode.RC500);
        }
        return R.success(space);
    }
}
