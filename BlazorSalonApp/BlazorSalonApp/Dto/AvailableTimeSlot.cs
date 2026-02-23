namespace BlazorSalonApp.Dto;

public class AvailableTimeSlot
{
    public DateTime StartTime { get; set; }
    public DateTime EndTime { get; set; }
    public bool Available { get; set; }
}
