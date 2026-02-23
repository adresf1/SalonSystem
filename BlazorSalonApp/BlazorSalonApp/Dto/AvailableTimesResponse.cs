namespace BlazorSalonApp.Dto;

public class AvailableTimesResponse
{
    public DateTime Date { get; set; }
    public List<AvailableTimeSlot> TimeSlots { get; set; } = new();
}
