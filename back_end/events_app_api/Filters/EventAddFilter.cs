using events_app_api.Data;
using events_app_api.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.EntityFrameworkCore;
using System.Text;

namespace events_app_api.Filters
{
    public class EventAddFilter : IAsyncActionFilter
    {
        public static async Task<bool> IsLinkValidAsync(string url)
        {
            if (!Uri.TryCreate(url, UriKind.Absolute, out var uriResult) ||
                (uriResult.Scheme != Uri.UriSchemeHttp && uriResult.Scheme != Uri.UriSchemeHttps))
            {
                return false; // Invalid URI format or unsupported scheme
            }

            using var httpClient = new HttpClient();
            httpClient.Timeout = TimeSpan.FromSeconds(5);

            try
            {
                var response = await httpClient.SendAsync(new HttpRequestMessage(HttpMethod.Head, url));
                return response.IsSuccessStatusCode;
            }
            catch
            {
                return false; // Request failed (e.g., DNS failure, timeout, etc.)
            }
        }

        private readonly ApiContext _context;

        public EventAddFilter(ApiContext context)
        {
            _context = context;
        }
        private void validateName(string? name, StringBuilder errorMessage)
        {
            if (string.IsNullOrEmpty(name) || name.Length < 5 || name.Length > 60)
            {
                errorMessage.AppendLine("The event name must have between 5 and 60 characters");
            }
        }
        private void validateDescription(string? description, StringBuilder errorMessage)
        {
            if (string.IsNullOrEmpty(description) || description.Length < 20 || description.Length > 500)
            {
                errorMessage.AppendLine("The event description must have between 20 and 500 characters");
            }
        }
        private async Task validateLocationIdStr(string? locationIdStr, StringBuilder errorMessage)
        {
            try
            {
                int locationId = int.Parse(locationIdStr);
                List<Location> locations = await _context.Locations.ToListAsync();
                if (!locations.Any(l => l.Id == locationId))
                {
                    errorMessage.AppendLine("Location with provided LocationId doesn't exist in the database");
                }
            }
            catch (Exception e)
            {
                errorMessage.AppendLine("Non-Integer or empty value provided for LocationId");
            }
        }
        private void validateCategoriesStr(string?[] categoriesStrs, StringBuilder errorMessage)
        {
            if(categoriesStrs == null || categoriesStrs.Length == 0)
            {
                errorMessage.AppendLine("No category provided");
                return;
            }
            foreach (string? categoryStr in categoriesStrs)
            {
                try
                {
                    int category = int.Parse(categoryStr);
                    if (category < 1 || category > 13)
                    {
                        errorMessage.AppendLine("Each element in the categories array must be between 1 and 13");
                        break;
                    }
                }
                catch (Exception e)
                {
                    errorMessage.AppendLine("One or more null/invalid categories were entered");
                    break;
                }
            }
        }
        private void validateFiles(IFormFileCollection? files,  StringBuilder errorMessage)
        {
            if(files == null || files.Count == 0)
            {
                errorMessage.AppendLine("No files provided");
            }
            foreach (var file in files)
            {
                string extension = Path.GetExtension(file.FileName);
                if (extension != ".jpg" && extension != ".jpeg" && extension != ".png" && extension != ".webp")
                {
                    errorMessage.AppendLine("Only image files accepted");
                    break;
                }
                if (file.Length / 1024.0 / 1024.0 > 5)
                {
                    errorMessage.AppendLine("One or more images are over 5MB in size. This is not allowed.");
                    break;
                }
            }
        }
        private async Task validateLink(string? link, StringBuilder errorMessage)
        {
            if (link == null)
            {
                errorMessage.AppendLine("Link is mandatory.");
            }
            else if (!await IsLinkValidAsync(link))
            {
                errorMessage.AppendLine("You inputted an invalid link.");
            }
        }
        private void validateDates(string? startDateStr, string? endDateStr,  StringBuilder errorMessage)
        {
            if (startDateStr == null && endDateStr == null)
            {
                errorMessage.AppendLine("Start/End date are mandatory fields");
            }
            bool isStartValid = DateTime.TryParse(startDateStr, out DateTime startDate);
            bool isEndValid = DateTime.TryParse(endDateStr, out DateTime endDate);
            if (!isStartValid || !isEndValid)
            {
                errorMessage.AppendLine("Invalid start/end date");
            }
            else
            {
                if (startDate >= endDate)
                {
                    errorMessage.AppendLine("Start date can't be equal or after end date");
                }
                if (startDate < DateTime.Now)
                {
                    errorMessage.AppendLine("Start date must be into the future");
                }
            }
        }
        public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
        {
            StringBuilder errorMessage = new StringBuilder();
            var request = context.HttpContext.Request;
            request.EnableBuffering();
                
            if (request.Method == HttpMethods.Post)
            {
                var form = await request.ReadFormAsync();
                string? name = form["Name"];        
                validateName(name, errorMessage);
                //between 5 and 60 chars

                string? description = form["Description"];
                validateDescription(description, errorMessage);
                //between 20 and 500 chars

                string? locationIdStr = form["LocationId"];
                await validateLocationIdStr(locationIdStr, errorMessage);
                //integer, not empty, must exist in db

                var categoriesStrs = form["Categories"];
                validateCategoriesStr(categoriesStrs, errorMessage);
                //integers between 1 and 13, non empty array

                var files = form.Files;
                validateFiles(files, errorMessage);
                //non empty, less than 5mb
                //extension != ".jpg" && extension != ".jpeg" && extension != ".png" && extension != ".webp"

                string? link = form["Link"];
                await validateLink(link, errorMessage);
                //non null, valid link

                string? startDateStr = form["StartDate"];
                string? endDateStr = form["EndDate"];
                validateDates(startDateStr, endDateStr, errorMessage);
                //non null, parceable by datetime, startDate can't be into the future, endDate must be after startDate
            }
            else if(request.Method == HttpMethods.Put)
            {
                var form = await request.ReadFormAsync();
                if (form.ContainsKey("Name"))
                {
                    string? name = form["Name"];
                    validateName(name, errorMessage);
                    //between 5 and 60 chars
                }

                if (form.ContainsKey("Description"))
                {
                    string? description = form["Description"];
                    validateDescription(description, errorMessage);
                    //between 20 and 500 chars
                }

                if (form.ContainsKey("LocationId"))
                {
                    string? locationIdStr = form["LocationId"];
                    await validateLocationIdStr(locationIdStr, errorMessage);
                    //integer, not empty, must exist in db
                }

                if (form.ContainsKey("Categories"))
                {

                    var categoriesStrs = form["Categories"];
                    validateCategoriesStr(categoriesStrs, errorMessage);
                    //integers between 1 and 13, non empty array
                }

                if (form.Files != null && form.Files.Count > 0)
                {
                    var files = form.Files;
                    validateFiles(files, errorMessage);
                    //non empty, less than 5mb
                    //extension != ".jpg" && extension != ".jpeg" && extension != ".png" && extension != ".webp"
                }

                if (form.ContainsKey("Link"))
                {
                    string? link = form["Link"];
                    await validateLink(link, errorMessage);
                    //non null, valid link
                }

    
                if(form.ContainsKey("StartDate") || form.ContainsKey("EndDate")){
                    string? startDateStr = form["StartDate"];
                    string? endDateStr = form["EndDate"];
                    validateDates(startDateStr, endDateStr, errorMessage);
                    //non null, parceable by datetime, startDate can't be into the future, endDate must be after startDate
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
