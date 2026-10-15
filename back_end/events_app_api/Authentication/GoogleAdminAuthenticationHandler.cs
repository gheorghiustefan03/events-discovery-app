using Google.Apis.Auth;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;
using System.Security.Claims;
using System.Text.Encodings.Web;

namespace events_app_api.Authentication
{
    public class GoogleAdminAuthenticationHandler : AuthenticationHandler<AuthenticationSchemeOptions>
    {
        private readonly IConfiguration _configuration;

        public GoogleAdminAuthenticationHandler(
            IOptionsMonitor<AuthenticationSchemeOptions> options,
            ILoggerFactory logger,
            UrlEncoder encoder,
            IConfiguration configuration) : base(options, logger, encoder)
        {
            _configuration = configuration;
        }

        protected override async Task<AuthenticateResult> HandleAuthenticateAsync()
        {

            string? authHeader = Request.Headers["Authorization"];

            if (string.IsNullOrWhiteSpace(authHeader))
            {
                // Continue as anonymous; protected endpoints must reject this.
                return AuthenticateResult.NoResult();
            }

            if (!authHeader.StartsWith(
                    "Bearer ", StringComparison.OrdinalIgnoreCase))
            {
                return AuthenticateResult.Fail("Invalid Authorization header.");
            }

            var token = authHeader["Bearer ".Length..].Trim();

            if (string.IsNullOrWhiteSpace(token))
            {
                return AuthenticateResult.Fail("Missing bearer token.");
            }

            var clientId = _configuration["GoogleAuth:ClientID"];
            var adminEmail = _configuration["GoogleAuth:AllowedAdminEmail"];

            if(string.IsNullOrWhiteSpace(clientId) || string.IsNullOrWhiteSpace(adminEmail))
            {
                throw new Exception("Missing GoogleAuth config");
            }

            try
            {
                var payload = await GoogleJsonWebSignature.ValidateAsync(token, new GoogleJsonWebSignature.ValidationSettings { Audience = [clientId] });

                if (payload.EmailVerified && string.Equals(payload.Email, adminEmail, StringComparison.OrdinalIgnoreCase))
                {
                    var claims = new List<Claim>
                    {
                        new Claim(ClaimTypes.NameIdentifier, payload.Subject),
                        new Claim(ClaimTypes.Email, payload.Email),
                        new Claim(ClaimTypes.Name, payload.Name ?? payload.Email)
                    };

                    var identity = new ClaimsIdentity(claims, "GoogleJwt");
                    var principal = new ClaimsPrincipal(identity);
                    var ticket = new AuthenticationTicket(principal, Scheme.Name);
                    return AuthenticateResult.Success(ticket);
                }
                else
                {
                    return AuthenticateResult.Fail("Google user unauthorized");
                }
            }
            catch (InvalidJwtException)
            {
                    return AuthenticateResult.Fail("Invalid Google token or wrong audience");
            }
            catch(Exception e)
            {
                throw new Exception(e.Message);
            }
        }
    }
}
