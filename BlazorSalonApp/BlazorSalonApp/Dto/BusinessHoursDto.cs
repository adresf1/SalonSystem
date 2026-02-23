namespace BlazorSalonApp.Dto;

public class BusinessHoursDto
{
    public long? Id { get; set; }
    public DayOfWeek DayOfWeek { get; set; }
    public bool IsOpen { get; set; }
    public TimeSpan? OpenTime { get; set; }
    public TimeSpan? CloseTime { get; set; }
    public TimeSpan? BreakStartTime { get; set; }
    public TimeSpan? BreakEndTime { get; set; }
}
