﻿namespace BlazorSalonApp.Autha;
using Blazored.LocalStorage;
using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;

public class AuthStateProvider : AuthenticationStateProvider
{
    private readonly ILocalStorageService _localStorage;
    private ClaimsPrincipal _anonymous = new(new ClaimsIdentity());

    public AuthStateProvider(ILocalStorageService localStorage)
    {
        _localStorage = localStorage;
    }

    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        try
        {
            var token = await _localStorage.GetItemAsync<string>("jwt_token");
            var role = await _localStorage.GetItemAsync<string>("user_role");
            var username = await _localStorage.GetItemAsync<string>("username");

            if (string.IsNullOrEmpty(token))
            {
                return new AuthenticationState(_anonymous);
            }

            // Create claims
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, username ?? ""),
                new Claim(ClaimTypes.Role, role ?? "")
            };

            var identity = new ClaimsIdentity(claims, "jwt");
            var user = new ClaimsPrincipal(identity);

            return new AuthenticationState(user);
        }
        catch
        {
            return new AuthenticationState(_anonymous);
        }
    }

    public void NotifyAuthenticationStateChanged()
    {
        NotifyAuthenticationStateChanged(GetAuthenticationStateAsync());
    }

    public async Task MarkUserAsLoggedOut()
    {
        await _localStorage.RemoveItemAsync("jwt_token");
        await _localStorage.RemoveItemAsync("user_role");
        await _localStorage.RemoveItemAsync("username");
        
        NotifyAuthenticationStateChanged();
    }
}