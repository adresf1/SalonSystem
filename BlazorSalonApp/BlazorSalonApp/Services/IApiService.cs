namespace BlazorSalonApp.Services;
using BlazorSalonApp.Dto;

public interface IApiService
{
    // Auth
    Task<AuthResponse> LoginAsync(LoginRequest request);
    
    // Admin - Business Management
    Task<BusinessWithOwnerResponse> CreateBusinessAsync(BusinessRequest request);
    Task<List<BusinessResponse>> GetAllBusinessesAsync();
    Task<BusinessResponse> GetBusinessBySlugAsync(string slug);
    Task<BusinessResponse> UpdateBusinessStatusAsync(long id, bool active);
    
    // Business Owner - My Business
    Task<BusinessResponse> GetMyBusinessAsync();
    
    // Business Owner - Services
    Task<List<ServiceResponse>> GetMyServicesAsync();
    Task<ServiceResponse> AddServiceAsync(ServiceRequest request);
    Task<ServiceResponse> UpdateServiceAsync(long serviceId, ServiceRequest request);
    Task DeleteServiceAsync(long serviceId);
    
    // Business Owner - Bookings
    Task<List<BookingResponse>> GetAllMyBookingsAsync();
    Task<List<BookingResponse>> GetTodayBookingsAsync();
    Task<List<BookingResponse>> GetBookingsByDateAsync(DateTime date);
    Task<BookingResponse> CompleteBookingAsync(long bookingId);
    Task<BookingResponse> CancelBookingAsync(long bookingId);
    
    // Public - Booking
    Task<List<ServiceResponse>> GetPublicServicesAsync(string businessSlug);
    Task<BookingResponse> CreatePublicBookingAsync(string businessSlug, BookingRequest request);
}