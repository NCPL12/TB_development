package ncpl.bms.reports.service;

import ncpl.bms.reports.model.dto.FloorToEnergyMeterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloorToEnergyMeterService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public FloorToEnergyMeterDTO getFloorDetailsById(int floorId) {
        try {
            String sql = "SELECT id, name, floor_id FROM floor_to_energy_meter_relation WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{floorId}, (rs, rowNum) -> {
                FloorToEnergyMeterDTO floor = new FloorToEnergyMeterDTO();
                floor.setId(rs.getInt("id"));
                floor.setName(rs.getString("name")); // Ensure name is stored
                floor.setFloorId(rs.getInt("floor_id"));
                return floor;
            });
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Floor not found with ID: " + floorId);
        }
    }
    public List<FloorToEnergyMeterDTO> getAllActiveEnergyMeters() {
        String sql = "SELECT id, name, floor_id FROM floor_to_energy_meter_relation";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            FloorToEnergyMeterDTO dto = new FloorToEnergyMeterDTO();
            dto.setId(rs.getInt("id"));
            dto.setName(rs.getString("name")); // Ensure name is retrieved
            dto.setFloorId(rs.getInt("floor_id"));
            return dto;
        });
    }
    public FloorToEnergyMeterDTO addEnergyMeter(FloorToEnergyMeterDTO energyMeter) {
        if (energyMeter.getName() == null || energyMeter.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Energy meter name cannot be null or empty");
        }

        String sql = "INSERT INTO floor_to_energy_meter_relation (name, floor_id) OUTPUT INSERTED.id, INSERTED.name, INSERTED.floor_id VALUES (?, ?)";
        return jdbcTemplate.queryForObject(sql, new Object[]{energyMeter.getName(), energyMeter.getFloorId()}, (rs, rowNum) -> {
            FloorToEnergyMeterDTO dto = new FloorToEnergyMeterDTO();
            dto.setId(rs.getInt("id"));
            dto.setName(rs.getString("name"));
            dto.setFloorId(rs.getInt("floor_id"));
            return dto;
        });
    }
    public void updateEnergyMeter(FloorToEnergyMeterDTO energyMeter) {
        String sql = "UPDATE floor_to_energy_meter_relation SET name = ?, floor_id = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, energyMeter.getName(), energyMeter.getFloorId(), energyMeter.getId());
        if (rowsAffected == 0) {
            throw new RuntimeException("Energy meter not found with ID: " + energyMeter.getId());
        }
    }

    public void deleteEnergyMeterById(int id) {
        String sql = "DELETE FROM floor_to_energy_meter_relation WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new RuntimeException("Energy meter not found with ID: " + id);
        }
    }
}
