package ncpl.bms.reports.controller;

import ncpl.bms.reports.model.dto.FloorToEnergyMeterDTO;
import ncpl.bms.reports.service.FloorToEnergyMeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("v1/floor-energy-meter")
@CrossOrigin(origins = "http://localhost:4200")
public class FloorToEnergyMeterController {

    @Autowired
    private FloorToEnergyMeterService floorToEnergyMeterService;

    @GetMapping("/get-all")
    public ResponseEntity<List<FloorToEnergyMeterDTO>> getAllFloorEnergyMeters() {
        List<FloorToEnergyMeterDTO> floorEnergyMeters = floorToEnergyMeterService.getAllActiveEnergyMeters();
        return ResponseEntity.ok(floorEnergyMeters);
    }

    @GetMapping("/floor-detail")
    public ResponseEntity<FloorToEnergyMeterDTO> getFloorDetails(@RequestParam int floorId) {
        return ResponseEntity.ok(floorToEnergyMeterService.getFloorDetailsById(floorId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addEnergyMeter(@RequestBody FloorToEnergyMeterDTO energyMeter) {
        try {
            if (energyMeter.getName() == null || energyMeter.getFloorId() == null) {
                return ResponseEntity.badRequest().body("Name and Floor ID are required");
            }
            FloorToEnergyMeterDTO savedEnergyMeter = floorToEnergyMeterService.addEnergyMeter(energyMeter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEnergyMeter);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving energy meter: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateEnergyMeter(@RequestBody FloorToEnergyMeterDTO energyMeter) {
        try {
            floorToEnergyMeterService.updateEnergyMeter(energyMeter);
            return ResponseEntity.ok("Energy meter updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the energy meter.");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteEnergyMeter(@PathVariable int id) {
        Map<String, String> response = new HashMap<>();
        try {
            floorToEnergyMeterService.deleteEnergyMeterById(id);
            response.put("message", "Energy meter deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
