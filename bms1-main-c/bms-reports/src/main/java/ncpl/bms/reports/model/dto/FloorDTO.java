package ncpl.bms.reports.model.dto;

public class FloorDTO {

    private Integer id;
    private String floorName;

    // Default constructor
    public FloorDTO() {}
    // Parameterized constructor
    public FloorDTO(Integer id, String floorName) {
        this.id = id;
        this.floorName = floorName;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }
}
