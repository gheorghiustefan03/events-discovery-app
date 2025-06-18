using Google.Apis.Auth;
using System.Security.Claims;

namespace events_app_api.Middleware
{
    public class GoogleAuthMiddleware
    {
        private readonly RequestDelegate _next;

        public GoogleAuthMiddleware(RequestDelegate next)
        {
            _next = next;
        }

        public async Task InvokeAsync(HttpContext context)
        {
            
            string authHeader = context.Request.Headers["Authorization"];

            if (!string.IsNullOrEmpty(authHeader) && authHeader.StartsWith("Bearer "))
            {
                var token = authHeader.Substring("Bearer ".Length).Trim();

                try
                {
                    var payload = await GoogleJsonWebSignature.ValidateAsync(token);
                    if (payload.Email == "gheorghiu.stefan.nicolae.cngrc@gmail.com")
                    {
                        var claims = new List<Claim>
                    {
                        new Claim(ClaimTypes.NameIdentifier, payload.Subject),
                        new Claim(ClaimTypes.Email, payload.Email),
                        new Claim(ClaimTypes.Name, payload.Name ?? payload.Email)
                    };

                        var identity = new ClaimsIdentity(claims, "GoogleJwt");
                        context.User = new ClaimsPrincipal(identity);
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("Invalid token: " + ex.Message);
                    context.Response.StatusCode = 403;
                    await context.Response.WriteAsync("Invalid Google token.");
                    return;
                }
            }
            

            await _next(context);
        }
    }
}
