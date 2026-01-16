namespace BlazorSalonApp.Dto;

public class BookingRequest
{
    public long ServiceId { get; set; }
    public DateTime StartTime { get; set; }
    public string CustomerName { get; set; } = string.Empty;
    public string CustomerPhone { get; set; } = string.Empty;
}