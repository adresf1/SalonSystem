namespace BlazorSalonApp.Dto;


public class ServiceRequest
{
    public string Name { get; set; } = string.Empty;
    public int DurationMinutes { get; set; }
    public decimal Price { get; set; }
}
