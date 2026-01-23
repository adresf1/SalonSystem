namespace BlazorSalonApp.Dto;

public class ClosedDateDto
{
    public long? Id { get; set; }
    public DateTime ClosedDate { get; set; }
    public string Reason { get; set; } = string.Empty;
}
