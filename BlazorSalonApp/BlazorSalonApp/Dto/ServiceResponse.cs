namespace BlazorSalonApp.Dto;
public class ServiceResponse
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public int DurationMinutes { get; set; }
    public decimal Price { get; set; }
    public bool Active { get; set; }
}