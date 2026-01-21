namespace BlazorSalonApp.Services;
using BlazorSalonApp.Dto;
using System.Net.Http.Json;


public class ApiService : IApiService
{
    private readonly HttpClient _http;
    private readonly HttpInterceptorService _interceptor;

    public ApiService(HttpClient http, HttpInterceptorService interceptor)
    {
        _http = http;
        _interceptor = interceptor;
    }

    // ============================================
    // AUTH
    // ============================================

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var response = await _http.PostAsJsonAsync("auth/login", request);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<AuthResponse>() 
            ?? throw new Exception("Login failed");
    }

   

    // ============================================
    // ADMIN - BUSINESS MANAGEMENT
    // ============================================

    public async Task<BusinessWithOwnerResponse> CreateBusinessAsync(BusinessRequest request)
    {
        var response = await _interceptor.PostAsync("admin/businesses", request);
        return await response.Content.ReadFromJsonAsync<BusinessWithOwnerResponse>() 
            ?? throw new Exception("Failed to create business");
    }

    public async Task<List<BusinessResponse>> GetAllBusinessesAsync()
    {
        var response = await _interceptor.GetAsync("admin/businesses");
        return await response.Content.ReadFromJsonAsync<List<BusinessResponse>>() 
            ?? new List<BusinessResponse>();
    }

    public async Task<BusinessResponse> GetBusinessBySlugAsync(string slug)
    {
        var response = await _interceptor.GetAsync($"admin/businesses/{slug}");
        return await response.Content.ReadFromJsonAsync<BusinessResponse>() 
            ?? throw new Exception("Business not found");
    }

    public async Task<BusinessResponse> UpdateBusinessStatusAsync(long id, bool active)
    {
        var response = await _interceptor.PatchAsync($"admin/businesses/{id}/status?active={active}");
        return await response.Content.ReadFromJsonAsync<BusinessResponse>() 
            ?? throw new Exception("Failed to update status");
    }

    // ============================================
    // BUSINESS OWNER - MY BUSINESS
    // ============================================

    public async Task<BusinessResponse> GetMyBusinessAsync()
    {
        var response = await _interceptor.GetAsync("business/my-business");
        return await response.Content.ReadFromJsonAsync<BusinessResponse>() 
            ?? throw new Exception("Failed to get business");
    }

    // ============================================
    // BUSINESS OWNER - SERVICES
    // ============================================

    public async Task<List<ServiceResponse>> GetMyServicesAsync()
    {
        var response = await _interceptor.GetAsync("business/services");
        return await response.Content.ReadFromJsonAsync<List<ServiceResponse>>() 
            ?? new List<ServiceResponse>();
    }

    public async Task<ServiceResponse> AddServiceAsync(ServiceRequest request)
    {
        var response = await _interceptor.PostAsync("business/services", request);
        return await response.Content.ReadFromJsonAsync<ServiceResponse>() 
            ?? throw new Exception("Failed to add service");
    }

    public async Task<ServiceResponse> UpdateServiceAsync(long serviceId, ServiceRequest request)
    {
        var response = await _interceptor.PutAsync($"business/services/{serviceId}", request);
        return await response.Content.ReadFromJsonAsync<ServiceResponse>() 
            ?? throw new Exception("Failed to update service");
    }

    public async Task DeleteServiceAsync(long serviceId)
    {
        await _interceptor.DeleteAsync($"business/services/{serviceId}");
    }

    // ============================================
    // BUSINESS OWNER - BOOKINGS
    // ============================================

    public async Task<List<BookingResponse>> GetAllMyBookingsAsync()
    {
        var response = await _interceptor.GetAsync("business/bookings");
        return await response.Content.ReadFromJsonAsync<List<BookingResponse>>() 
            ?? new List<BookingResponse>();
    }

    public async Task<List<BookingResponse>> GetTodayBookingsAsync()
    {
        var response = await _interceptor.GetAsync("business/bookings/today");
        return await response.Content.ReadFromJsonAsync<List<BookingResponse>>() 
            ?? new List<BookingResponse>();
    }

    public async Task<List<BookingResponse>> GetBookingsByDateAsync(DateTime date)
    {
        var dateStr = date.ToString("yyyy-MM-dd");
        var response = await _interceptor.GetAsync($"business/bookings/date?date={dateStr}");
        return await response.Content.ReadFromJsonAsync<List<BookingResponse>>() 
            ?? new List<BookingResponse>();
    }

    public async Task<BookingResponse> CompleteBookingAsync(long bookingId)
    {
        var response = await _interceptor.PatchAsync($"business/bookings/{bookingId}/complete");
        return await response.Content.ReadFromJsonAsync<BookingResponse>() 
            ?? throw new Exception("Failed to complete booking");
    }

    public async Task<BookingResponse> CancelBookingAsync(long bookingId)
    {
        var response = await _interceptor.PatchAsync($"business/bookings/{bookingId}/cancel");
        return await response.Content.ReadFromJsonAsync<BookingResponse>() 
            ?? throw new Exception("Failed to cancel booking");
    }

    // ============================================
    // PUBLIC - BOOKING
    // ============================================

    public async Task<List<ServiceResponse>> GetPublicServicesAsync(string businessSlug)
    {
        var response = await _http.GetAsync($"public/{businessSlug}/services");
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<List<ServiceResponse>>() 
            ?? new List<ServiceResponse>();
    }

    public async Task<BookingResponse> CreatePublicBookingAsync(string businessSlug, BookingRequest request)
    {
        var url = $"public/{businessSlug}/bookings";
        Console.WriteLine($"Creating booking at: {url}");
        Console.WriteLine($"Request data - ServiceId: {request.ServiceId}, StartTime: {request.StartTime}, Name: {request.CustomerName}, Phone: {request.CustomerPhone}");
        
        var response = await _http.PostAsJsonAsync(url, request);
        
        if (!response.IsSuccessStatusCode)
        {
            var errorContent = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"Booking failed with status {response.StatusCode}");
            Console.WriteLine($"Error response: {errorContent}");
            throw new HttpRequestException($"Booking failed ({response.StatusCode}): {errorContent}");
        }
        
        return await response.Content.ReadFromJsonAsync<BookingResponse>() 
            ?? throw new Exception("Failed to parse booking response");
    }

    public async Task<AvailableTimesResponse> GetAvailableTimeSlotsAsync(string businessSlug, DateTime date, long serviceId)
    {
        // Ensure we only use the date part, no time
        var dateOnly = date.Date;
        var dateStr = dateOnly.ToString("yyyy-MM-dd");
        var url = $"public/{businessSlug}/available-times?date={dateStr}&serviceId={serviceId}";
        
        Console.WriteLine($"Calling: {url}");
        
        var response = await _http.GetAsync(url);
        
        if (!response.IsSuccessStatusCode)
        {
            var errorContent = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"Error response: {errorContent}");
            throw new Exception($"Failed to get available times: {response.StatusCode}");
        }
        
        return await response.Content.ReadFromJsonAsync<AvailableTimesResponse>() 
            ?? throw new Exception("Failed to parse available times response");
    }
}