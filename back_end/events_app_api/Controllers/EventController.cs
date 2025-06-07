using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using events_app_api.Models;
using events_app_api.Data;
using System.ComponentModel;
using Microsoft.EntityFrameworkCore;
using System;
using System.IO;
using events_app_api.Filters;
using static System.Net.WebRequestMethods;
using Microsoft.AspNetCore.Authorization;
using System.Text.Json;
using System.Diagnostics.Eventing.Reader;
using Microsoft.IdentityModel.Tokens;

namespace events_app_api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [ServiceFilter(typeof(EventAddFilter))]
    public class EventController : ControllerBase
    {
        //creates images directory if it doesn't already exist
        //deletes all images in specific event directory
        //adds new images in folder
        //replaces imageUrls with correct references
        private async Task syncImages(Event @event, List<IFormFile> files, string? existingImagesOrderJson = null)
        {
            var existingImageOrder = new List<ImageOrder>();
            if (!string.IsNullOrWhiteSpace(existingImagesOrderJson))
            {
                existingImageOrder = JsonSerializer.Deserialize<List<ImageOrder>>(existingImagesOrderJson);
            }
            string eventImagesFolder = Path.Combine(_env.WebRootPath, "images");
            if (!Directory.Exists(eventImagesFolder)) Directory.CreateDirectory(eventImagesFolder);

            string path = Path.Combine(eventImagesFolder, @event.Id.ToString());
            if (Directory.Exists(path))
            {
                foreach (string existingFile in Directory.GetFiles(path))
                {
                    string fileName = Path.GetFileNameWithoutExtension(existingFile);
                    if (!existingImageOrder.Exists(io => io.id == int.Parse(fileName)))
                        System.IO.File.Delete(existingFile);
                    else
                    {
                        string newPath = Path.Combine(path, "temp_" + existingImageOrder.Find(io => io.id == int.Parse(fileName)).index.ToString()) + Path.GetExtension(existingFile);
                        System.IO.File.Move(existingFile, newPath);
                    }
                }
                foreach(string existingFile in Directory.GetFiles(path))
                {
                    string fileName = Path.GetFileNameWithoutExtension(existingFile);
                    if (fileName.Contains("temp_"))
                    {
                        System.IO.File.Move(existingFile, Path.Combine(path, fileName.Replace("temp_", "")) + Path.GetExtension(existingFile));
                    }
                }
                foreach (string subDirectory in Directory.GetDirectories(path))
                {
                    Directory.Delete(subDirectory, true);
                }

            }
            else Directory.CreateDirectory(path);
            List<string> imageUrls = new List<string>();
            int skipped = 0;
            for (int i = 0; i < files.Count() + existingImageOrder.Count(); i++)
            {
                if (existingImageOrder.Any(io => io.index == i))
                {
                    string existingFileExtension = Path.GetExtension(Path.GetFileName(@event.ImageUrls.First(iu => Path.GetFileName(iu).Contains(i.ToString()))));
                    imageUrls.Add(Path.Join("http://localhost:5073", "images", @event.Id.ToString(), i.ToString()) + existingFileExtension
    + $"?v={DateTime.UtcNow.Ticks}");
                    skipped += 1;
                    continue;
                }

                string filename = Path.Combine(path, i.ToString()) + Path.GetExtension(Path.GetFileName(files[i - skipped].FileName));
                using (var stream = new FileStream(filename, FileMode.Create))
                {
                    await files[i - skipped].CopyToAsync(stream);
                }
                imageUrls.Add(Path.Join("http://localhost:5073", "images", @event.Id.ToString(), i.ToString()) + Path.GetExtension(Path.GetFileName(files[i - skipped].FileName))
                    + $"?v={DateTime.UtcNow.Ticks}");
            }
            @event.ImageUrls = imageUrls;
        }
        private readonly ApiContext _context;
        private readonly IWebHostEnvironment _env;
        public EventController(ApiContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }
        [HttpPost]
        [Authorize]
        public async Task<IActionResult> CreateEvent([FromForm]Event @event, [FromForm]List<IFormFile> files)
        {
            try
            {
                await _context.Events.AddAsync(@event);

                await syncImages(@event, files);

                await _context.SaveChangesAsync();

                return Ok(@event);
            }catch (Exception ex)
            {
                return Problem("Server error");
            }
        }
        record ImageOrder(int id, int index);
        [HttpPut("{id}")]
        [Authorize]
        public async Task<IActionResult> UpdateEvent(int id, [FromForm] Event @event, [FromForm] List<IFormFile> files, [FromForm(Name = "existingImageOrder")] string existingImageOrderJson)
        {
            try
            {

                Event? ev = await _context.Events.FindAsync(id);
                if (ev != null)
                {
                    if(@event.Name != null)
                        ev.Name = @event.Name;
                    if (@event.Description != null)
                        ev.Description = @event.Description;
                    if (@event.LocationId != default)
                    {
                        ev.LocationId = @event.LocationId;
                        await _context.Entry(ev).Reference(e => e.Location).LoadAsync();
                    }
                    if (@event.Categories != null)
                        ev.Categories = @event.Categories;
                    if (@event.Link != null)
                        ev.Link = @event.Link;
                    if (@event.StartDate != default)
                        ev.StartDate = @event.StartDate;
                    if (@event.EndDate != default)
                        ev.EndDate = @event.EndDate;

                    if ((files != null && files.Count > 0) || !existingImageOrderJson.IsNullOrEmpty())
                    {
                        await syncImages(ev, files, existingImageOrderJson);
                        //await _context.SaveChangesAsync();
                    }
                    await _context.SaveChangesAsync();

                    return Ok(ev);
                }
                else return NotFound("Event not found");
            } catch(Exception ex)
            {
                return Problem("Server error");
            }
        }
        [HttpGet("{id}")]
        public async Task<IActionResult> getEvent(int id)
        {
            try
            {
                Event? _event = await _context.Events.FindAsync(id);
                if (_event != null)
                    return Ok(_event);
                else return NotFound();
            }
            catch (Exception e)
            {
                return Problem("Server error");
            }
        }
        [HttpDelete("{id}")]
        [Authorize]
        public async Task<IActionResult> deleteEvent(int id)
        {
            try
            {
                Event? ev = await _context.Events.FindAsync(id);
                if (ev == null) return NotFound();

                //ev.Location.Events.Remove(ev);
                _context.Events.Remove(ev);
                await _context.SaveChangesAsync();
                return Ok("Delete success");
            }
            catch(Exception ex)
            {
                return Problem("Server error");
            }

        }
        [HttpGet]
        public async Task<IActionResult> getAllEvents([FromQuery(Name = "lId")] int[] locationIds)
        {
            List<Event> events = await _context.Events.ToListAsync();
            List<int> locationIdsList = new List<int>(locationIds);
            return Ok(events.Where(e => locationIdsList.Find(lId => lId == e.LocationId) != default));
        }
        [HttpGet("categories")]
        public async Task<IActionResult> getRandomCategories()
        {
            var categories = new List<Category>(4);
            Category[] allCategories = (Category[])Enum.GetValues(typeof(Category));
            Random rnd = new Random();
            for(int i = 0; i < 4; i++)
            {
                Category category = allCategories[rnd.Next(0, allCategories.Length)];
                if (!categories.Contains(category)) categories.Add(category);
                else i--;
            }
            return Ok(categories);
        }
    }
}
