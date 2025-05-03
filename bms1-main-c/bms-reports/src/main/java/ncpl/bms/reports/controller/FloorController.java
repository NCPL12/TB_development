package ncpl.bms.reports.controller;

import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.model.dto.FloorDTO;
import ncpl.bms.reports.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class FloorController {

    @Autowired
    private FloorService floorService;

    @PostMapping("/add-floor")
    public FloorDTO addFloor(@RequestBody FloorDTO floorDTO) {
        return floorService.addFloor(floorDTO);
    }

    @PutMapping("/update-floor/{id}")
    public FloorDTO updateFloor(@PathVariable Integer id, @RequestBody FloorDTO floorDTO) {
        return floorService.updateFloor(id, floorDTO);
    }

    @DeleteMapping("/delete-floor/{id}")
    public ResponseEntity<Map<String, String>> deleteFloor(@PathVariable Integer id) {
        floorService.deleteFloor(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Floor deleted successfully");
        return ResponseEntity.ok(response);
    }

        @GetMapping("/get-all-floors")
    public List<FloorDTO> getAllFloors() {
        return floorService.getAllFloors();
    }
}
