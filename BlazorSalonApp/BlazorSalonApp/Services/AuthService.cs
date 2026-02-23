namespace BlazorSalonApp.Services;
using BlazorSalonApp.Dto;
using Blazored.LocalStorage;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using BlazorSalonApp.Autha;
public class AuthService
{
    private readonly IApiService _apiService;
    private readonly ILocalStorageService _localStorage;
    private readonly AuthStateProvider _authStateProvider;

    public AuthService(
        IApiService apiService, 
        ILocalStorageService localStorage,
        AuthStateProvider authStateProvider)
    {
        _apiService = apiService;
        _localStorage = localStorage;
        _authStateProvider = authStateProvider;
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var response = await _apiService.LoginAsync(request);
        
        // Save token and user info
        await _localStorage.SetItemAsync("jwt_token", response.Token);
        await _localStorage.SetItemAsync("user_role", response.Role);
        await _localStorage.SetItemAsync("username", response.Username);
        
        // Notify auth state changed
        _authStateProvider.NotifyAuthenticationStateChanged();
        
        return response;
    }

    public async Task LogoutAsync()
    {
        await _localStorage.RemoveItemAsync("jwt_token");
        await _localStorage.RemoveItemAsync("user_role");
        await _localStorage.RemoveItemAsync("username");
        
        _authStateProvider.NotifyAuthenticationStateChanged();
    }

    public async Task<string?> GetTokenAsync()
    {
        return await _localStorage.GetItemAsync<string>("jwt_token");
    }

    public async Task<string?> GetRoleAsync()
    {
        return await _localStorage.GetItemAsync<string>("user_role");
    }

    public async Task<bool> IsAuthenticatedAsync()
    {
        var token = await GetTokenAsync();
        return !string.IsNullOrEmpty(token);
    }
}

// ============================================
// HTTP INTERCEPTOR SERVICE
// ============================================

public class HttpInterceptorService
{
    private readonly HttpClient _http;
    private readonly ILocalStorageService _localStorage;

    public HttpInterceptorService(HttpClient http, ILocalStorageService localStorage)
    {
        _http = http;
        _localStorage = localStorage;
    }

    private async Task<HttpRequestMessage> CreateRequestWithTokenAsync(
        HttpMethod method, 
        string url, 
        object? content = null)
    {
        var request = new HttpRequestMessage(method, url);
        
        // Add JWT token
        var token = await _localStorage.GetItemAsync<string>("jwt_token");
        if (!string.IsNullOrEmpty(token))
        {
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        }
        
        // Add content if provided
        if (content != null)
        {
            request.Content = JsonContent.Create(content);
        }
        
        return request;
    }

    public async Task<HttpResponseMessage> GetAsync(string url)
    {
        var request = await CreateRequestWithTokenAsync(HttpMethod.Get, url);
        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
        return response;
    }

    public async Task<HttpResponseMessage> PostAsync(string url, object content)
    {
        var request = await CreateRequestWithTokenAsync(HttpMethod.Post, url, content);
        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
        return response;
    }

    public async Task<HttpResponseMessage> PutAsync(string url, object content)
    {
        var request = await CreateRequestWithTokenAsync(HttpMethod.Put, url, content);
        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
        return response;
    }

    public async Task<HttpResponseMessage> PatchAsync(string url, object? content = null)
    {
        var request = await CreateRequestWithTokenAsync(HttpMethod.Patch, url, content);
        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
        return response;
    }

    public async Task<HttpResponseMessage> DeleteAsync(string url)
    {
        var request = await CreateRequestWithTokenAsync(HttpMethod.Delete, url);
        var response = await _http.SendAsync(request);
        response.EnsureSuccessStatusCode();
        return response;
    }
}