package ncpl.bms.reports.model.dto;

public class FloorToEnergyMeterDTO {
    private int id;
    private Integer floorId;
    private String name;

    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Integer getFloorId() {
        return floorId;
    }
    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
