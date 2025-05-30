using events_app_api.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using System.Runtime.CompilerServices;
using System.Text;
using System.Text.Json;
using System.Xml.Linq;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace events_app_api.Filters
{
    public class LocationAddFilter : IAsyncActionFilter
    {

        private void validateName(string? name, StringBuilder errorMessage)
        {
            if (string.IsNullOrEmpty(name) || name.Length < 5 || name.Length > 60)
            {
                errorMessage.AppendLine("The location name must have between 5 and 60 characters");
            }
        }
        private void validateLatLong(string? latStr, string?  lngStr, StringBuilder errorMessage)
        {
            if(string.IsNullOrEmpty(latStr) || string.IsNullOrEmpty(lngStr))
            {
                errorMessage.AppendLine("Lat and Long can't be empty");
                return;
            }
            bool validLat = double.TryParse(latStr, out double lat);
            bool validLng = double.TryParse(lngStr, out double lng);
            if(!validLat || !validLng)
            {
                errorMessage.AppendLine("Non-double values inputted for lat and lng");
                return;
            }
            if(lat < -90 || lat > 90 || lng < -180 || lng > 180)
            {
                errorMessage.AppendLine("Lat must be between -90 and 90, Lng between -180 and 180");
            }
        }
        public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
        {
            StringBuilder errorMessage = new StringBuilder();
            var request = context.HttpContext.Request;
            request.EnableBuffering();
            Location? locationObj;

            if(request.Method == HttpMethods.Post || request.Method == HttpMethods.Put)
            {
                if (!(context.ActionArguments.TryGetValue("location", out var locationGenericObj) && locationGenericObj is Location location))
                {
                    errorMessage.AppendLine("Invalid or missing location data.");
                }
                locationObj = (Location)locationGenericObj;
                if (request.Method == HttpMethods.Post)
                {

                    string? name = locationObj.Name;
                    validateName(name, errorMessage);

                    string? latitude = locationObj.Latitude.ToString();
                    string? longitude = locationObj.Longitude.ToString();
                    validateLatLong(latitude, longitude, errorMessage);
                }
                else if (request.Method == HttpMethods.Put)
                {
                    if (!string.IsNullOrEmpty(locationObj.Name))
                    {
                        validateName(locationObj.Name, errorMessage);
                    }
                    if (locationObj.Latitude.HasValue || locationObj.Longitude.HasValue)
                    {
                        validateLatLong(locationObj.Latitude.ToString(), locationObj.Latitude.ToString(), errorMessage);
                    }
                }
            }

            var errorStr = errorMessage.ToString();
            if (string.IsNullOrEmpty(errorStr))
            {
                await next();
            }
            else
            {
                context.Result = new BadRequestObjectResult(new { error = errorStr.Split("\r\n").Where(e => !string.IsNullOrEmpty(e)) });
            }
        }
    }
}



