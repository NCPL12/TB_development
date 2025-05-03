package ncpl.bms.reports.service;

import ncpl.bms.reports.model.dto.FloorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class FloorService{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public FloorDTO addFloor(FloorDTO floorDTO) {
        String sql = "INSERT INTO floors (floor_name) VALUES (?)";
        jdbcTemplate.update(sql, floorDTO.getFloorName());
        return floorDTO;
    }

    public FloorDTO updateFloor(Integer id, FloorDTO floorDTO) {
        String sql = "UPDATE floors SET floor_name = ? WHERE id = ?";
        jdbcTemplate.update(sql, floorDTO.getFloorName(), id);
        floorDTO.setId(id);
        return floorDTO;
    }
    public void deleteFloor(Integer id) {
        String sql = "DELETE FROM floors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    public List<FloorDTO> getAllFloors() {
        String sql = "SELECT id, floor_name FROM floors";
        return jdbcTemplate.query(sql, new RowMapper<FloorDTO>() {
            @Override
            public FloorDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new FloorDTO(
                        rs.getInt("id"),
                        rs.getString("floor_name")
                );
            }
        });
    }
}
