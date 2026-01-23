using BlazorSalonApp.Components;
using BlazorSalonApp.Services;
using BlazorSalonApp.Autha;
using Microsoft.AspNetCore.Components.Authorization;

using Blazored.LocalStorage;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

// Configure HttpClient
builder.Services.AddScoped(sp =>
{
    var apiBaseUrl = builder.Configuration.GetValue<string>("ApiBaseUrl") ?? "http://localhost:8080";
    
    return new HttpClient
    {
        BaseAddress = new Uri($"{apiBaseUrl}/api/")
    };
});

// Add services
builder.Services.AddScoped<HttpInterceptorService>();
builder.Services.AddScoped<IApiService, ApiService>();
builder.Services.AddScoped<AuthService>();

// Add authentication
builder.Services.AddCascadingAuthenticationState();
builder.Services.AddScoped<AuthStateProvider>();
builder.Services.AddScoped<AuthenticationStateProvider>(provider => 
    provider.GetRequiredService<AuthStateProvider>());
builder.Services.AddAuthorizationCore();

// Add local storage
builder.Services.AddBlazoredLocalStorage();
var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}

app.UseHttpsRedirection();

app.UseAntiforgery();

app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

app.Run();
