namespace BlazorSalonApp.Dto;

public class BusinessResponse
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Slug { get; set; } = string.Empty;
    public bool Active { get; set; }
    public string BookingUrl { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
}